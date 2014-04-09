package com.whereismydot.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModelRunner implements Runnable{

	private final Map<String, Double> features;
	private final List<List<Double>>  prices;


	private final List<Model<Map<String, Double>, Double>>  regressionModels;
	private final List<Model<Map<String, Double>, Boolean>> classificationModels;

	public static void main(String[] args) {
		
		if(args.length < 2){
			System.out.println("The first paramter should be the path to the "
					+ "feature vectors followed paths to stock prices.");
			return;
		}
		
		// Load all the data
		Map<String, Double> features;
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
		
		// Specify which models should be evaluated.
		List<Model<Map<String, Double>, Double>> regressionModels
			= new ArrayList<Model<Map<String, Double>, Double>>();
	
		regressionModels.add(new GaussianProcess<Map<String, Double>>(null, 0));
		
		List<Model<Map<String, Double>, Boolean>> classificationnModels
			= new ArrayList<Model<Map<String, Double>, Boolean>>();
		
		new ModelRunner(features, prices, regressionModels, classificationnModels).run();
		
		System.out.println("Done");
	}
	
	public ModelRunner(Map<String, Double> features, List<List<Double>> prices,
			List<Model<Map<String, Double>, Double>> regressionModels,
			List<Model<Map<String, Double>, Boolean>> classificationModels){
		this.features = features;
		this.prices   = prices;
		
		this.regressionModels     = regressionModels;
		this.classificationModels = classificationModels;
	}

	@Override
	public void run() {
		for(List<Double> stock : prices){
			for(Model<Map<String, Double>, Double> model : regressionModels){
				// TODO Train the model on the subset of the data and evaluate 
				// the prediction for different subsets of the data.
			}
			
			for(Model<Map<String, Double>, Boolean> model : classificationModels){
				// TODO Train the model on the subset of the data and evaluate 
				// the prediction for different subsets of the data.
			}
		}	
		
		
	}
	
	static private Map<String, Double> readFeatureVectors(String path) throws IOException{
		//TODO
		return null;
	}
	
	static private List<Double> readStockPrices(String path)throws IOException{
		//TODO
		return null;
	}
	

	
}
