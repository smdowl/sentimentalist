package com.whereismydot.models;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.summary.Product;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.whereismydot.utils.StockDataLoader;
import com.whereismydot.utils.StockDataLoader.PriceType;

public class ModelRunner implements Runnable{

	private final List<List<Map<String, Double>>> features;
	private final List<List<Double>>  prices;
	
	private final int foldSize;
	private final int foldCount;
	
	private final List<Model<Map<String, Double>, Double>>  regressionModels;
	private final List<Model<Map<String, Double>, Boolean>> classificationModels;

	
	/**
	 * A simple container class to reference a subset of the data sets for use 
	 * in k-fold validation 
	 * @author andrey
	 *
	 */
	public static class Fold<T>{
		public final List<T> prices;
		public final List<Map<String, Double>> features;
		public final Map<String, Double> x; 
		public final T y;
		
		public Fold(List<Map<String, Double>> features, List<T> prices, 
				Map<String, Double> x, T y){
			this.features = features;
			this.prices   = prices;
			this.x 		  = x;
			this.y 	      = y;
		}
	}
	
	public static void main(String[] args) {
		
		if(args.length < 2 || args.length % 2 != 0){
			System.out.println("The arguments should be pairs of paths to features and prices.");
			return;
		}
		
		LocalDate from = new LocalDate(2014, 1, 27);
		LocalDate to   = new LocalDate(2014, 3, 4); 
		
		// Load all the data
		List<List<Map<String, Double>>> features = new ArrayList<List<Map<String, Double>>>();
		List<List<Double>>  prices = new ArrayList<List<Double>>();
		
		StockDataLoader loader = new StockDataLoader(from, to, 1, PriceType.Close);

		try{
			for(int i = 0; i < args.length; i += 2){
				features.add(readFeatureVectors(args[i], from, to));
				prices.add(loader.load(args[i + 1]));
			}
			
		}catch(IOException ex){
			System.out.println("Error reading input files:" + ex.getMessage());
			return;
		}
		
		// Specify which regression models should be evaluated.
		List<Model<Map<String, Double>, Double>> regressionModels
			= new ArrayList<Model<Map<String, Double>, Double>>();

		regressionModels.add(new LastValueModel<Map<String, Double>, Double>());
		regressionModels.add(new GaussianProcess(new Kernels.Linear(), 0.2));

		regressionModels.add(new GaussianProcess(new Kernels.Gaussian(100), 0.2));
		
		regressionModels.add(new GaussianProcess(new Kernels.WaveKernel(15), 0.2));
		
		
		ArrayList<Kernel<Map<String,Double>>> kernels = new ArrayList<Kernel<Map<String,Double>>>();
		kernels.add(new Kernels.Gaussian(100));
		kernels.add(new Kernels.WaveKernel(15));
		regressionModels.add(new GaussianProcess(new Kernels.ProductKernel<Map<String,Double>>(kernels), 0.2));
		
//        regressionModels.add(new LinearRegression<Map<String,Double>>(new MatrixBuilders.MatrixBuilderLinear()));
        regressionModels.add(new SVM<Map<String,Double>>(new SVMtrains.simple()));

		
		//Go
		new ModelRunner(features, prices, regressionModels, new ArrayList<Model<Map<String, Double>, Boolean>>()).run();
		
	}
	
	public ModelRunner(List<List<Map<String, Double>>> features, List<List<Double>> prices,
			List<Model<Map<String, Double>, Double>> regressionModels,
			List<Model<Map<String, Double>, Boolean>> classificationModels){
		this.features = features;
		this.prices   = prices;
		
		this.regressionModels     = regressionModels;
		this.classificationModels = classificationModels;

		// Set how many folds to run what fold size to use. 
		this.foldSize  = 20;
		this.foldCount = 30;
		
	}

	@Override
	public void run() {
		
		runRegressionModels();
		
//		System.out.println("\n--------------------------------------------------------------------------------\n");
//		
//		double[][] classificationResults = runClassificationModels();
//		printResult("Classification results", classificationResults);
	}
	
	private void runRegressionModels(){
		double[][] results 		   = new double[prices.size()][regressionModels.size()];
		double[][] directionResult = new double[prices.size()][regressionModels.size()];
		
		for(int stockIdx = 0; stockIdx < prices.size(); stockIdx++){
			for(int foldIdx = 0; foldIdx < foldCount; foldIdx++){

				Fold<Double> fold = randomRegressionFold(foldSize, stockIdx);
			
				for(int modelIdx = 0; modelIdx < regressionModels.size(); modelIdx++){
					Model<Map<String, Double>, Double> model = regressionModels.get(modelIdx);
					
//					setOutputEnabled(false);
					model.train(fold.features, fold.prices);
					
					double prediction = model.predict(fold.x);
//					setOutputEnabled(true);
//					System.out.println("Pred:" + prediction + " actual:" + fold.y);
					double error = fold.y - prediction;
				
					results[stockIdx][modelIdx] += error * error;
				
					// Check if the price moved in the right direction
					double prevPrice = fold.prices.get(fold.prices.size() - 1);
					if(fold.y >= prevPrice && prediction >= prevPrice
							|| fold.y <= prevPrice && prediction <= prevPrice){
					
						directionResult[stockIdx][modelIdx] += 1;
					}
				}
			}			
		}
		setOutputEnabled(true);
		
		//Normalize the MSE and direction results
		for(int i = 0; i < results.length; i++){
			for(int j = 0; j < results[i].length; j++){
				results[i][j] 	      /= foldCount;
				directionResult[i][j] /= foldCount;
			}
		}	
		
		//Compute means 
		double[][] meanMSE = new double[1][regressionModels.size()];
		for(int model = 0; model < regressionModels.size(); model++){
			for(int stock = 0; stock < prices.size(); stock++){
				meanMSE[0][model] += results[stock][model] / ((double) regressionModels.size());
			}
		}
		
		double[][] meanDirection = new double[1][regressionModels.size()];
		for(int model = 0; model < regressionModels.size(); model++){
			for(int stock = 0; stock < prices.size(); stock++){
				meanDirection[0][model] += directionResult[stock][model] / ((double) regressionModels.size());
			}
		}
				
		// Print the results
		printResult("Regression", results);
		System.out.println("\n--------------------------------------------------------------------------------\n");
		
		printResult("Regression (Mean)", meanMSE);

		System.out.println("\n--------------------------------------------------------------------------------\n");

		// Print the results
		printResult("Regression (Direction Only)", directionResult);

		System.out.println("\n--------------------------------------------------------------------------------\n");

		printResult("Regression (Direction Only) (Mean)", meanDirection);


	}
	
	private double[][] runClassificationModels(){
		double[][] results = new double[prices.size()][classificationModels.size()];
		
		for(int stockIdx = 0; stockIdx < prices.size(); stockIdx++){
			Fold<Boolean> fold = randomClassificationFold(foldSize, stockIdx);
			
			for(int modelIdx = 0; modelIdx < classificationModels.size(); modelIdx++){
				
				Model<Map<String, Double>, Boolean> model = classificationModels.get(modelIdx);
				model.train(fold.features, fold.prices);
				
				double error = fold.y == model.predict(fold.x) ? 0 : 1;
				
				results[stockIdx][modelIdx] += error;
			}
			
			//Normalize the error rate 
			for(int i = 0; i < results.length; i++){
				for(int j = 0; j < results[i].length; j++){
					results[i][j] /= foldCount;
				}
			}
		}
		
		
		
		return results;
	}
	
	/**
	 * Converts a list of prices to a list of booleans indicating if the price
	 * has gone up or down since the last time step. True if the price has 
	 * increased.
	 * @param prices
	 * @return
	 */
	private List<Boolean> computeChanges(List<Double> prices){
		List<Boolean> result = new ArrayList<Boolean>(prices.size() - 1);
		for(int i = 1; i < prices.size(); i++){
			result.add(prices.get(i) > prices.get(i - 1));
		}
		return result;
	}
	
	/**
	 * Pretty print the results
	 * @param title
	 * @param results
	 */
	private void printResult(String title, double[][] results){
		System.out.println("Results for:" + title);
		
		int colWidth = 24;
		
		System.out.print(pad("", colWidth));
		for(int i = 0; i < results[0].length; i++){
			System.out.print(pad("| Model " + i, colWidth));
		}
		System.out.println();
		
		for(int stock = 0; stock < results.length; stock++){
			System.out.print(pad("Stock number " + stock, colWidth));
			for(int model = 0; model < results[stock].length; model++){
				System.out.print(pad("| " + results[stock][model], colWidth));
			}
			System.out.println();
		}
	}
	
	/**
	 * Pad a string to a given length. Used for printing results.
	 * @param str
	 * @param length
	 * @return
	 */
	private String pad(String str, int length){
		if(str.length() < length){
			int padding = length - str.length();
			for(int i = 0; i < padding; i++){
				str += " ";
			}
		}
		return str;
	}
	
	/**
	 * Returns a random subset of data to run analysis on.
	 * 
	 * @param size
	 * @param stockIdx
	 * @return
	 */
	private Fold<Double> randomRegressionFold(int size, int stockIdx){
//		System.out.print(features.size());
		int startIdx 		  = new Random().nextInt(features.get(stockIdx).size() - size - 1);
		
		List<Double> subPrice = prices.get(stockIdx).subList(startIdx,startIdx + size);
		List<Map<String, Double>> subFeatures = features.get(stockIdx).subList(startIdx, startIdx + size);
		
		Map<String,Double> x  = features.get(stockIdx).get(startIdx + size + 1);
		double			   y  = prices.get(stockIdx).get(startIdx + size + 1);
		
		return new Fold<Double>(subFeatures, subPrice, x, y);
	}
	
	private Fold<Boolean> randomClassificationFold(int size, int priceIdx){
		Fold<Double> fold = randomRegressionFold(size, priceIdx);
		
		List<Boolean> changes = computeChanges(fold.prices);
		List<Map<String, Double>> features = fold.features.subList(1, fold.features.size() - 1); 
		Boolean y = fold.prices.get(fold.prices.size() - 1) < fold.y;
		
		return new Fold<Boolean>(features, changes, fold.x, y);
		
	}
	
	private void setOutputEnabled(boolean enabled){
		if(enabled){
			System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));

		}else{
			System.setOut(new PrintStream(new OutputStream() {
				
				@Override
				public void write(int b) throws IOException {
					
				}
			}));
		}
	}
	
	
	/**
	 * This method assumes that the feature vectors are sorted with no gaps
	 * @param path
	 * @return
	 * @throws IOException
	 */
	static private List<Map<String, Double>> readFeatureVectors(String path, LocalDate from, LocalDate to) throws IOException{
		
		int totalDays = Days.daysBetween(from, to).getDays();
		
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
		
		@SuppressWarnings("unchecked")
		Map<String, Double>[] result = new Map[totalDays];
		
		JsonParser parser = new JsonParser();
		
		for(String line : lines){
			int splitIdx = line.indexOf("\t");
			
			long timestamp = Long.parseLong(line.substring(0, splitIdx));
			int  day       = Days.daysBetween(from, new LocalDate(timestamp)).getDays();
			if(day >= 0 && day < totalDays){	
			
				JsonElement json = parser.parse(line.substring(splitIdx));
				Map<String, Double> features = new HashMap<String, Double>();
			
				for(Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()){
					features.put(entry.getKey(), entry.getValue().getAsDouble());
				}
				if (result[day] != null){
					System.out.println("Multiple feature vectors for day:" + day);
				}
				result[day] = features;
			}		
		}
		
		return verifyFeatures(result);		
		
	}	
	
	static private List<Map<String, Double>> verifyFeatures(Map<String, Double>[] features){
		List<Map<String, Double>> result = new ArrayList<Map<String, Double>>(features.length);
		Set<Integer> missing = new TreeSet<Integer>();
		for(int i = 0; i < features.length; i++){
			if(features[i] == null){
				missing.add(i);
			}else{
				result.add(features[i]);
			}
		}
		
		if(missing.size() == 0 ){
			return result;	
		}else{		
			StringBuffer buf = new StringBuffer("Missing:");
			for(Integer i : missing){
				buf.append(" ").append(i);
			}
			
			throw new RuntimeException(buf.toString());
		}
	}
}
