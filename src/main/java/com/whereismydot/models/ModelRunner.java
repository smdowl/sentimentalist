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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
	public static class Fold{
		public final List<Double> prices;
		public final List<Map<String, Double>> features;
		public final Map<String, Double> x; 
		public final Double y;
		
		public Fold(List<Map<String, Double>> features, List<Double> prices, 
				Map<String, Double> x, Double y){
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
		try{ 
			features = readFeatureVectors(args[0]);
			
			prices   = new ArrayList<List<Double>>();
			for(int i = 1; i < args.length; i++){
				prices.add(readStockPrices(args[i]));
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

		// Specify which classification models should be evaluated.
		List<Model<Map<String, Double>, Boolean>> classificationnModels
			= new ArrayList<Model<Map<String, Double>, Boolean>>();
		
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
		
		double[][] regressionResults = new double[prices.size()][regressionModels.size()];
		
		for(int stockIdx = 0; stockIdx < prices.size(); stockIdx++){
			Fold fold = randomFold(foldSize, stockIdx);
			
			for(int modelIdx = 0; modelIdx < regressionModels.size(); modelIdx++){
				
				Model<Map<String, Double>, Double> model = regressionModels.get(modelIdx);
				model.train(fold.features, fold.prices);
				
				double error = fold.y - model.predict(fold.x);
				
				regressionResults[stockIdx][modelIdx] += error * error;
			}
			
			//Normalise the MSE 
			for(int i = 0; i < regressionResults.length; i++){
				for(int j = 0; j < regressionResults[i].length; j++){
					regressionResults[i][j] /= foldCount;
				}
			}
			
			for(Model<Map<String, Double>, Boolean> model : classificationModels){
				// TODO Train the model on the subset of the data and evaluate 
				// the prediction for different subsets of the data.
			}
			
			printResult("Regression results", regressionResults);
		}		
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
	private Fold randomFold(int size, int priceIdx){
		int startIdx 		  = new Random().nextInt(features.size() - size - 1);
		
		List<Double> subPrice = prices.get(priceIdx).subList(startIdx,startIdx + size);
		List<Map<String, Double>> subFeatures = features.subList(startIdx, startIdx + size);
		
		Map<String,Double> x  = features.get(startIdx + size + 1);
		double			   y  = prices.get(priceIdx).get(startIdx + size + 1);
		
		return new Fold(subFeatures, subPrice, x, y);
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
	
	static private List<Double> readStockPrices(String path)throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
		List<Double> result = new ArrayList<Double>(lines.size());
		for(String line : lines){
			result.add(Double.valueOf(line));
		}
		return result;
	}	
}
