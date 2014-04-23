package com.whereismydot.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * This class loads CSV price data generated by Yahoo finance and returns clean 
 * Lists of prices.
 * @author andrey
 *
 */
public class StockDataLoader {

	private final LocalDate from;
	private final LocalDate to;
	private final int  step;
	private final int priceCol;
	
	public enum PriceType{
		Open, High, Low, Close
	}
	
	public StockDataLoader(LocalDate from, LocalDate to, int step, PriceType type) {
		super();
		this.from = from;
		this.to = to;
		this.step = step;
		
		priceCol = priceColumn(type);		
	}
	
	public List<Double> load(String path) throws IOException{
		
		int totalDays = Days.daysBetween(from, to).getDays();
		Double[] result = new Double[totalDays];
		List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);

		// Skip the header
		lines = lines.subList(1, lines.size() - 1);
		
		for(String line : lines){
			String[] tokens = line.split(",");

			// Ignore dividend lines
			if(tokens.length == 7){
				LocalDate date = LocalDate.parse(tokens[0]);
				
				int day = Days.daysBetween(from, date).getDays();
				
				// Ignore days outside the time range
				if (day >= 0 && day < totalDays){
					result[day] = Double.parseDouble(tokens[priceCol]);
				}
			}
		}
		
		// Interpolate missing days. Since missing days do not have any trading 
		// taking place (eg. weekends) just use the previous day price. 
		if(result[0] == null){
			throw new RuntimeException("No data for first day - can't interpolate");
		}
		
		for(int i = 1; i < result.length; i++){
			if(result[i] == null){
				result[i] = result[i - 1];
			}
		}		
		
		return resample(result);
	}
	
	/**
	 * Used to subsample the price history 
	 * @param prices
	 * @return
	 */
	private List<Double> resample(Double[] prices){
		List<Double> result = new ArrayList<Double>(prices.length);
		for(int i = 0; i < prices.length; i+= step){
			result.add(prices[i]);
		}
		return result;
	}
	
	private int priceColumn(PriceType type){
		switch (type) {
			case Open:  return 1;
			case High:  return 2;
			case Low:   return 3;
			case Close: return 4;
			default: throw new RuntimeException("WTF totality checker");
		}
	}
	
}