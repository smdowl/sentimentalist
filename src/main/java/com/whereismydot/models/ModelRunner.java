package com.whereismydot.models;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.joda.time.LocalDate;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.whereismydot.utils.StockDataLoader;
import com.whereismydot.utils.StockDataLoader.PriceType;

public class ModelRunner implements Runnable{

	private final List<Map<String, Double>> features;
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
		
		if(args.length < 2){
			System.out.println("The first paramter should be the path to the "
					+ "feature vectors followed paths to stock prices.");
			return;
		}
		
		// Load all the data
		List<Map<String, Double>> features;
		List<List<Double>>  prices;
		StockDataLoader loader = new StockDataLoader(new LocalDate(2014, 1, 14), 
										new LocalDate(2014, 3, 14), 1, PriceType.Close);

		try{ 
			features = readFeatureVectors(args[0]);
			
			prices   = new ArrayList<List<Double>>();
			for(int i = 1; i < args.length; i++){
				prices.add(loader.load(args[i]));
			}
			
		}catch(IOException ex){
			System.out.println("Error reading input files:" + ex.getMessage());
			return;
		}
		
		// Specify which regression models should be evaluated.
		List<Model<Map<String, Double>, Double>> regressionModels
			= new ArrayList<Model<Map<String, Double>, Double>>();

		regressionModels.add(new LastValueModel<Map<String, Double>, Double>());
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.Linear(), 0));

		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.Gaussian(1), 0));
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.Gaussian(10), 0));
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.Gaussian(100), 0));
		
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.WaveKernel(1), 0));
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.WaveKernel(10), 0));
		regressionModels.add(new GaussianProcess<Map<String, Double>>(new Kernels.WaveKernel(100), 0));
        regressionModels.add(new LinearRegression<Map<String, Double>>(new MatrixBuilders.MatrixBuilderLinear()));


		// Specify which classification models should be evaluated.
		List<Model<Map<String, Double>, Boolean>> classificationnModels
			= new ArrayList<Model<Map<String, Double>, Boolean>>();
		
		classificationnModels.add(new LastValueModel<Map<String,Double>, Boolean>());
		
		//Go
		new ModelRunner(features, prices, regressionModels, classificationnModels).run();
		
	}
	
	public ModelRunner(List<Map<String, Double>> features, List<List<Double>> prices,
			List<Model<Map<String, Double>, Double>> regressionModels,
			List<Model<Map<String, Double>, Boolean>> classificationModels){
		this.features = features;
		this.prices   = prices;
		
		this.regressionModels     = regressionModels;
		this.classificationModels = classificationModels;

		// Set how many folds to run what fold size to use. 
		this.foldSize  = 200;
		this.foldCount = 100;
		
	}

	@Override
	public void run() {
		
		double[][] regressionResults = runRegressionModels();
		printResult("Regression results", regressionResults);
		
		System.out.println("\n--------------------------------------------------------------------------------\n");
		
		double[][] classificationResults = runClassificationModels();
		printResult("Classification results", classificationResults);
	}
	
	private double[][] runRegressionModels(){
		double[][] results = new double[prices.size()][regressionModels.size()];

		for(int stockIdx = 0; stockIdx < prices.size(); stockIdx++){
			Fold<Double> fold = randomRegressionFold(foldSize, stockIdx);
			
			for(int modelIdx = 0; modelIdx < regressionModels.size(); modelIdx++){
				
				Model<Map<String, Double>, Double> model = regressionModels.get(modelIdx);
				model.train(fold.features, fold.prices);
				
				double error = fold.y - model.predict(fold.x);
				
				results[stockIdx][modelIdx] += error * error;
			}
			
			//Normalise the MSE 
			for(int i = 0; i < results.length; i++){
				for(int j = 0; j < results[i].length; j++){
					results[i][j] /= foldCount;
				}
			}
		}
		return results;
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
			
			//Normalise the error rate 
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
	 * @param priceIdx
	 * @return
	 */
	private Fold<Double> randomRegressionFold(int size, int priceIdx){
		int startIdx 		  = new Random().nextInt(features.size() - size - 1);
		
		List<Double> subPrice = prices.get(priceIdx).subList(startIdx,startIdx + size);
		List<Map<String, Double>> subFeatures = features.subList(startIdx, startIdx + size);
		
		Map<String,Double> x  = features.get(startIdx + size + 1);
		double			   y  = prices.get(priceIdx).get(startIdx + size + 1);
		
		return new Fold<Double>(subFeatures, subPrice, x, y);
	}
	
	private Fold<Boolean> randomClassificationFold(int size, int priceIdx){
		Fold<Double> fold = randomRegressionFold(size, priceIdx);
		
		List<Boolean> changes = computeChanges(fold.prices);
		List<Map<String, Double>> features = fold.features.subList(1, fold.features.size() - 1); 
		Boolean y = fold.prices.get(fold.prices.size() - 1) < fold.y;
		
		return new Fold<Boolean>(features, changes, fold.x, y);
		
	}
	
	
	/**
	 * This method assumes that the feature vectors are sorted with no gaps
	 * @param path
	 * @return
	 * @throws IOException
	 */
	static private List<Map<String, Double>> readFeatureVectors(String path) throws IOException{
		
		
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
		List<Map<String, Double>> result = new ArrayList<Map<String, Double>>(lines.size());
		JsonParser parser = new JsonParser();
		
		for(String line : lines){
			int splitIdx = line.indexOf(" ");
			
			JsonElement json = parser.parse(line.substring(splitIdx));
			Map<String, Double> features = new HashMap<String, Double>();
			
			for(Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()){
				features.put(entry.getKey(), entry.getValue().getAsDouble());
			}
			
			result.add(features);
		
		}
		
		return result;
	}
		
}
