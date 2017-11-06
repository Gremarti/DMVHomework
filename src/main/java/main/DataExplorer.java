package main;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class DataExplorer {

	public static String workingDirectory = "/home/toshuumilia/tmp/testDMV/";
	public static String rawdatasetProduct = workingDirectory + "order_products__train.csv";
	public static String rawSequenceDataset = workingDirectory + "transactions_seq.txt";

	public static String transactionsetProduct = workingDirectory + "trainProduct.transaction";
	public static String transactionsetAisle = workingDirectory + "trainAisle.transaction";
	public static String transactionsetSequence = workingDirectory + "customer.sequence";

	public static String productInformation = workingDirectory + "products.csv";
	public static String aisleInformation = workingDirectory + "aisles.csv";
	public static String sequenceInformation = workingDirectory + "customer.seqinfo";

	public static String lcmPatterns = workingDirectory + "pattern.lcm";
	public static String aprioriPatterns = workingDirectory + "pattern.apriori";
	public static String bideplusPatterns = workingDirectory + "pattern.bideplus";


	public static void main(String[] args) {
		Set<Integer> itemsItemsets = new HashSet<>();
		Set<Integer> itemsSupport = new HashSet<>();
		Set<Integer> itemsSequence = new HashSet<>();
		Set<Integer> antecedents = new HashSet<>();
		Set<Integer> consequents = new HashSet<>();
		Set<Integer> aisles = new HashSet<>();
		Set<Integer> itemExclusions = new HashSet<>();

		// Create the dataset in the transaction format.
		DatasetConverter.convertCSVIntoTransaction(rawdatasetProduct, transactionsetProduct);
		DatasetConverter.sortTransaction(transactionsetProduct);

		// Find the mamximum support in the product transaction set.
		checkMaxSupport(transactionsetProduct);

		// Replace the product id with its aisle id.
		DatasetConverter.replaceIdByAisle(productInformation, rawdatasetProduct, transactionsetAisle);
		DatasetConverter.sortTransaction(transactionsetAisle);

		// Find the maximum support in the aisle transaction set
		checkMaxSupport(transactionsetAisle);

		// Tell how much items in the *product* transaction set has a support lower than 1% of the highest one.
		separateItemOccurrence(workingDirectory +"itemOccurrence.csv", getItemOccurrence(transactionsetProduct), 187.27);
		// Tell how much items in the *aisle* transaction set has a support lower than 1% of the highest one.
		separateItemOccurrence(workingDirectory +"itemOccurrence.csv", getItemOccurrence(transactionsetAisle), 721.28);


		// Find 100 itemests with the highest support and with at least 2 items in each itemsets.
		findMaxSupportItemsets(lcmPatterns, 100, 1);

		// Find the itemsets with the items I want.
		itemsItemsets.addAll(Arrays.asList(24,83,120,123));
		chooseItemset(lcmPatterns, itemsItemsets);

		// Compute the support of an itemset
		itemsSupport.addAll(Arrays.asList(24, 83));
		computeSupport(itemsSupport, transactionsetAisle);

		// Compute the confidence of an association rule
		antecedents.addAll(Arrays.asList(24, 83, 120));
		consequents.addAll(Arrays.asList(123));
		computeConfidence(antecedents, consequents, transactionsetAisle);

		// Get the name of some aisles
		aisles.addAll(Arrays.asList(24, 83));
		findNameAisle(aisles, aisleInformation);

		// Create the sequence dataset
		DatasetConverter.convertCSVIntoSequences(rawSequenceDataset, transactionsetSequence, sequenceInformation);
		DatasetConverter.sortSequences(transactionsetSequence);

		//Find the 10 most supported sequences containing at least 3 itemsets with at least one having 2 items, and exclude all sequences having the provided items.
		itemExclusions.addAll(Arrays.asList(93, 474, 6, 66));
		findMaxSupportSequence(bideplusPatterns, 10, 2, 2, itemExclusions);

		itemsSequence.addAll(Arrays.asList(75, 251));
		findNameProductSeq(itemsSequence, sequenceInformation);
	}

	public static Map<Integer, Integer> getItemOccurrence(String transactionPath){
		Map<Integer, Integer> mapItemOccurrence = new HashMap<>();

		if(transactionPath.contains(".transaction")){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(transactionPath));
				String line;
				int numberTransaction = 0;

				while ((line = reader.readLine()) != null){
					String[] lineSplit = line.split(" ");

					for(String split : lineSplit){
						try {
							Integer item = Integer.valueOf(split);

							Integer numberOccurrence = mapItemOccurrence.getOrDefault(item, 0) + 1;

							mapItemOccurrence.put(item, numberOccurrence);
						} catch (NumberFormatException e){
							System.err.println("NumberFormatException");
						}
					}

					numberTransaction++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mapItemOccurrence;
	}

	public static void checkMaxSupport(String transactionPath){
		if(transactionPath.contains(".transaction")){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(transactionPath));
				Map<Integer, Integer> mapItemOccurrence = new HashMap<>();
				String line;
				int numberTransaction = 0;
				int maxSupport = 0;
				int idMaxSupport = -1;

				while ((line = reader.readLine()) != null){
					String[] lineSplit = line.split(" ");

					for(String split : lineSplit){
						try {
							Integer item = Integer.valueOf(split);

							Integer numberOccurrence = mapItemOccurrence.getOrDefault(item, 0) + 1;

							mapItemOccurrence.put(item, numberOccurrence);
							if (maxSupport < numberOccurrence) {
								maxSupport = numberOccurrence;
								idMaxSupport = item;
							}
						} catch (NumberFormatException e){
							System.err.println("NumberFormatException");
						}
					}

					numberTransaction++;
				}

				System.out.println("Number of Transactions: "+ numberTransaction);
				System.out.println("Most present item: "+ idMaxSupport + " ("+ maxSupport +" items)");
				System.out.println("Max relative support for Apriori: "+ ((double) maxSupport)/numberTransaction);

				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException();
		}
	}

	public static void separateItemOccurrence(String output, Map<Integer, Integer> mapItemOccurrence, double threshold){
		int[] category = new int[2];
		double separator = 721.78;

		for(Integer itemId : mapItemOccurrence.keySet()){
			int index = mapItemOccurrence.get(itemId) < threshold ? 0 : 1;
			category[index]++;
		}

		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));

			writer.write("interval,occurrence");
			writer.newLine();

			writer.write("[0.00;"+ threshold +"[,"+ category[0]);
			writer.newLine();

			writer.write("["+ threshold +";"+ (int) (threshold*100) +"],"+ category[1]);
			writer.newLine();

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void chooseItemset(String patternPath, Set<Integer> itemsNeeded){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(patternPath));
			StringBuilder stringBuilder = new StringBuilder("");
			String line;

			while((line = reader.readLine()) != null){
				String[] linePart = line.split(" #SUP: ");
				String[] items = linePart[0].split(" ");
				Set<Integer> recognizedItems = new HashSet<>();

				for(String item : items){
					Integer item_id = Integer.valueOf(item);
					if(itemsNeeded.contains(item_id)){
						recognizedItems.add(item_id);
					}
				}

				if(recognizedItems.size() == itemsNeeded.size()){
					stringBuilder.append(line);
					stringBuilder.append('\n');
				}
			}

			System.out.println(stringBuilder.toString());

			reader.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void computeSupport(Set<Integer> itemset, String dataset){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataset));
			String line;
			int support = 0;

			while ((line = reader.readLine()) != null){
				String[] lineParts = line.split(" ");
				Set<Integer> recognizedItems = new HashSet<>();

				for(String item : lineParts){
					Integer item_id = Integer.valueOf(item);
					if(itemset.contains(item_id)){
						recognizedItems.add(item_id);
					}
				}

				if(recognizedItems.size() == itemset.size()){
					support++;
				}
			}

			System.out.println(prettyprintItemset(itemset) + ": Supp="+ support);

			reader.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void computeConfidence(Set<Integer> antecedents, Set<Integer> consequents, String dataset){
		Set<Integer> antUcon = new HashSet<>();
		double antUconSupport = 0;
		double antSupport = 0;

		antUcon.addAll(antecedents);
		antUcon.addAll(consequents);

		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataset));
			String line;

			while((line = reader.readLine()) != null){
				String[] items = line.split(" ");
				Set<Integer> recognizedAntItems = new HashSet<>();
				Set<Integer> recognizedAntUConItems = new HashSet<>();

				for(String item : items){
					Integer item_id = Integer.valueOf(item);

					if(antecedents.contains(item_id)){
						recognizedAntItems.add(item_id);
					}
					if(antUcon.contains(item_id)){
						recognizedAntUConItems.add(item_id);
					}
				}

				if(recognizedAntItems.size() == antecedents.size()){
					antSupport++;
				}
				if(recognizedAntUConItems.size() == antUcon.size()){
					antUconSupport++;
				}
			}

			// Pretty print
			StringBuilder stringBuilderAnt = new StringBuilder("");
			for(Integer item_id : antecedents){
				stringBuilderAnt.append(item_id);
				stringBuilderAnt.append(',');
			}
			stringBuilderAnt.deleteCharAt(stringBuilderAnt.lastIndexOf(","));

			// Pretty print
			StringBuilder stringBuilderCon = new StringBuilder("");
			for(Integer item_id : consequents){
				stringBuilderCon.append(item_id);
				stringBuilderCon.append(',');
			}
			stringBuilderCon.deleteCharAt(stringBuilderCon.lastIndexOf(","));

			System.out.println("{"+ stringBuilderAnt +"} -> {"+ stringBuilderCon +"}: Conf="+ antUconSupport / antSupport);
			System.out.println("Antecedents support: "+ antSupport);
			System.out.println("Antecedents U Consequents support: "+ antUconSupport);

			reader.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void findMaxSupportItemsets(String patternPath, int n, int excludePatternSizeLessThan){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(patternPath));
			String line;

			Map<String, Integer> mapISSupport = new HashMap<>();

			while((line = reader.readLine()) != null){
				String[] lineParts = line.split(" #SUP: ");
				String[] items = lineParts[0].split(" ");

				if(items.length > excludePatternSizeLessThan) {
					Integer support = Integer.valueOf(lineParts[1]);
					SortedSet<Integer> itemset = new TreeSet<>();

					for (String item_id : items) {
						itemset.add(Integer.valueOf(item_id));
					}

					mapISSupport.put(prettyprintItemset(itemset), support);
				}
			}
			reader.close();

			System.out.println(mapISSupport.keySet().size() +" interesting patterns found.");
			List<String> nMaxItemset = findMaxSupportMap(new HashMap<>(mapISSupport), n);

			for(String itemsetStr : nMaxItemset) {
				System.out.println(itemsetStr);
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void findMaxSupportSequence(String patternPath, int n, int excludeSequenceSizeLessThan, int sizeOneItemsetAtLeast, Set<Integer> excludeItems){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(patternPath));
			String line;

			Map<String, Integer> mapSSupport = new HashMap<>();

			while((line = reader.readLine()) != null){
				String[] lineParts = line.split(" #SUP: ");
				String[] itemsets = lineParts[0].split("-1");

				if(itemsets.length > excludeSequenceSizeLessThan) {
					Integer support = Integer.valueOf(lineParts[1]);

					boolean itemExcluded = false;
					boolean itemsetSizeExcluded = true;

					for(String itemset : itemsets){
						String[] items = itemset.split(" ");
						int nbItems = 0;

						for(String item : items){
							if(!item.equals("")){
								int itemID = Integer.valueOf(item);

								for(Integer excludedItem : excludeItems) {
									if (excludedItem.equals(itemID)){
										itemExcluded = true;
										break;
									}
								}

								nbItems++;
							}
						}

						if(nbItems >= sizeOneItemsetAtLeast){
							itemsetSizeExcluded = false;
						}
					}

					if(!itemExcluded && !itemsetSizeExcluded) {
						mapSSupport.put(prettyprintSequence(lineParts[0]), support);
					}
				}
			}
			reader.close();

			System.out.println(mapSSupport.keySet().size() +" interesting patterns found.");

			List<String> nMaxItemset = findMaxSupportMap(new HashMap<>(mapSSupport), n);

			for(String itemsetStr : nMaxItemset) {
				System.out.println(itemsetStr);
			}
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static List<String> findMaxSupportMap(Map<String, Integer> mapSupport, int n){
		List<String> nMaxItemset = new ArrayList<>();

		for(int i = 0; i < n; i++){
			String itemsetMax = "";
			Integer supportMax = -1;

			for(String itemset : mapSupport.keySet()){
				Integer support = mapSupport.get(itemset);
				if(supportMax < support){
					itemsetMax = itemset;
					supportMax = support;
				}
			}

			if(!itemsetMax.equals("")) {
				nMaxItemset.add(itemsetMax + "-Supp=" + supportMax);
				mapSupport.remove(itemsetMax);
			}
		}

		return nMaxItemset;
	}

	public static void findNameAisle(Set<Integer> aislesNeeded, String aisleInformations){
		try {
			StringBuilder stringBuilder = new StringBuilder("{*");
			CSVReader csvReader = new CSVReader(new FileReader(aisleInformations));
			String[] lineParts;

			csvReader.readNext(); // Trash attributes name line
			while((lineParts = csvReader.readNext()) != null){
				if(aislesNeeded.contains(Integer.valueOf(lineParts[0]))){
					stringBuilder.append(lineParts[1]);
					stringBuilder.append(", ");
				}
			}
			stringBuilder.delete(stringBuilder.lastIndexOf(","), stringBuilder.lastIndexOf(",")+2);
			stringBuilder.append("*}");

			System.out.println(stringBuilder);

			csvReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void findNameProductSeq(Set<Integer> productsNeeded, String sequenceInformationPath){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(sequenceInformationPath));
			String line;

			while((line = reader.readLine()) != null){
				String[] lineParts = line.split(",");
				Integer itemID = Integer.valueOf(lineParts[1]);

//				if((lineParts[0] +","+ lineParts[1]).contains(",93")){
//					System.out.println((lineParts[0] +","+ lineParts[1]));
//				}

				if(productsNeeded.contains(itemID)){
					System.out.println(lineParts[1] +"=>"+ lineParts[0]);
				}
			}

			reader.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	private static String prettyprintItemset(Set<Integer> itemset){
		StringBuilder stringBuilder = new StringBuilder("{");

		for(Integer item_id : itemset){
			stringBuilder.append(item_id);
			stringBuilder.append(',');
		}
		stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
		stringBuilder.append('}');

		return stringBuilder.toString();
	}

	private static String prettyprintSequence(String rawSequence){
		String[] itemsets = rawSequence.split("-1");
		List<Set<Integer>> sequence = new ArrayList<>();

		for(String rawitemset : itemsets){
			String[] items = rawitemset.split(" ");
			Set<Integer> itemset = new HashSet<>();

			for(String item : items){
				if(!item.equals("")){
					itemset.add(Integer.valueOf(item));
				}
			}

			sequence.add(itemset);
		}

		return prettyprintSequence(sequence);
	}

	private static String prettyprintSequence(List<Set<Integer>> sequence){
		StringBuilder stringBuilder = new StringBuilder("[");

		for(Set<Integer> itemset : sequence){
			stringBuilder.append(prettyprintItemset(itemset));
			stringBuilder.append(" ");
		}
		stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(" "));

		stringBuilder.append("]");
		return stringBuilder.toString();
	}

	//	public static void countNumberTransactionWithMoreThanNItem(String transactionPath, int n){
//		try{
//			BufferedReader reader = new BufferedReader(new FileReader(transactionPath));
//			String line;
//			int sum = 0;
//
//			while((line = reader.readLine()) != null){
//				sum += line.split(" ").length >= n ? 1 : 0;
//			}
//
//			System.out.println("Number of transactions with at least "+ n +" items: "+ sum);
//
//			reader.close();
//		} catch (IOException e){
//			e.printStackTrace();
//		}
//	}
	//	public static void countNumberItemInTransaction(File input, File output){
//		if(input.getAbsolutePath().contains(".transaction")) {
//			try {
//				BufferedReader reader = new BufferedReader(new FileReader(input));
//				BufferedWriter writer = new BufferedWriter(new FileWriter(output));
//				String line;
//
//				writer.write("length");
//				writer.newLine();
//
//				while ((line = reader.readLine()) != null) {
//					String[] lineSplit = line.split(" ");
//					writer.write(lineSplit.length +"");
//					writer.newLine();
//				}
//
//				writer.close();
//				reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		} else {
//			throw new RuntimeException("");
//		}
//	}

	//	public static void groupItemOccurrence(String output, Map<Integer, Integer> mapItemOccurrence){
//		Map<String, Integer> mapIntervalCount = new HashMap<>();
//		List<String> listInterval = new ArrayList<>();
//
//		DecimalFormat nFormat = new DecimalFormat("#.##");
//		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
//		dfs.setDecimalSeparator('.');
//		nFormat.setDecimalFormatSymbols(dfs);
//
//
//		int nbInterval = 100;
//		double max = 72178d +1;
//
//		for(int i = 0; i < nbInterval; i++){
//			String interval = "["+ nFormat.format((max/nbInterval)*i) +";"+ nFormat.format((max/nbInterval)*(i+1)) +"[";
//			listInterval.add(interval);
//			mapIntervalCount.put(interval, 0);
//		}
//
//		for(Integer itemId : mapItemOccurrence.keySet()){
//			String interval = listInterval.get((int) (mapItemOccurrence.get(itemId)/(max/nbInterval)));
//
//			mapIntervalCount.put(interval, mapIntervalCount.getOrDefault(interval, 0)+1);
//		}
//
//		try{
//			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
//
//			writer.write("interval,occurrence");
//			writer.newLine();
//
//			for(String key : listInterval){
//				if(!mapIntervalCount.get(key).equals(0)) {
//					writer.write(key + "," + mapIntervalCount.get(key));
//					writer.newLine();
//				}
//			}
//
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
