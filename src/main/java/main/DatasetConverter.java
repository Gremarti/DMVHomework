package main;

import com.opencsv.CSVReader;

import java.io.*;
import java.util.*;

public class DatasetConverter {

	public static void convertCSVIntoTransaction(String input, String output){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(input));
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));

			String line;
			int idOrder;
			int idProduct;


			int lastIdOrder = -1;
			boolean firstTransaction = true;

			// Delete first line with the header.
			reader.readLine();

			while ((line = reader.readLine()) != null){
				String[] lines = line.split(",");

				idOrder = Integer.valueOf(lines[0]);
				idProduct = Integer.valueOf(lines[1]);


				if(lastIdOrder != idOrder){
					if(firstTransaction){
						firstTransaction = false;
					} else {
						writer.write("\n");
					}

					lastIdOrder = idOrder;
				}

				writer.write(idProduct + " ");
			}

			reader.close();
			writer.close();
		} catch (IOException exception){
			exception.printStackTrace();
		}
	}

	public static void replaceIdByAisle(String product, String dataset, String output){
		Map<Integer, Integer> mapProductIdAisleId = new HashMap<>();

		try{
			Set<Integer> setAisleKept = new HashSet<>();
			BufferedReader reader;// = new BufferedReader(new FileReader(product));
			BufferedWriter writer;// = new BufferedWriter(new FileWriter(DataExplorer.workingDirectory + "replace.tmp"));
			String line;
			int idOrder;
			int previousIdOrder = -1;
			int idProduct;
			boolean firstTransaction = true;

			CSVReader csvreader = new CSVReader(new FileReader(product));
			String[] nline;

			csvreader.readNext();
			while ((nline = csvreader.readNext()) != null) {
				try {
					mapProductIdAisleId.put(Integer.valueOf(nline[0]), Integer.valueOf(nline[2]));
				} catch (NumberFormatException e){

					for(String l : nline) {
						System.out.print(l);
						if(!l.equals(nline[nline.length-1])) {
							System.out.print(",");
						}
					}
					System.out.println();
					throw e;
				}
			}

			reader = new BufferedReader(new FileReader(dataset));
			writer = new BufferedWriter(new FileWriter(output));

			reader.readLine();

			while ((line = reader.readLine()) != null){
				String[] lines = line.split(",");

				idOrder = Integer.valueOf(lines[0]);
				idProduct = Integer.valueOf(lines[1]);


				if(previousIdOrder != idOrder){
					if(firstTransaction){
						firstTransaction = false;
					} else {

						if(!setAisleKept.isEmpty()) {
							for (Integer idAisle : setAisleKept) {
								writer.write(idAisle.toString() + " ");
							}
							writer.write("\n");
						}

						setAisleKept.clear();
					}

					previousIdOrder = idOrder;
				}

				setAisleKept.add(mapProductIdAisleId.get(idProduct));
			}

			// For the last transaction.
			if(!setAisleKept.isEmpty()) {
				for (Integer idAisle : setAisleKept) {
					writer.write(idAisle.toString() + " ");
				}
				writer.write("\n");
			}

			reader.close();
			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void sortTransaction(String transactionPath){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(transactionPath));
			StringBuilder stringBuilder = new StringBuilder("");
			String line;

			while ((line = reader.readLine()) != null){
				String[] linePart = line.split(" ");
				SortedSet<Integer> sortedSet = new TreeSet<>();

				for(String item : linePart){
					sortedSet.add(Integer.valueOf(item));
				}

				for(Integer item_id : sortedSet){
					stringBuilder.append(item_id);
					stringBuilder.append(' ');
				}

				stringBuilder.append('\n');
			}

			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(transactionPath));

			writer.write(stringBuilder.toString());

			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void convertCSVIntoSequences(String rawSequenceDataset, String outputSequence, String outputInfo){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(rawSequenceDataset));
			BufferedWriter writerSequence = new BufferedWriter(new FileWriter(outputSequence));
			BufferedWriter writerInfo = new BufferedWriter(new FileWriter(outputInfo));

			String line;
			StringBuilder sequenceBuilder = new StringBuilder("");
			int lineNumber = 0;

			Map<String, Integer> mapNameId = new HashMap<>();
			int freeID = 1;
			int lastCustomerID = -1;
			int lastOrderNumber = -1;
			boolean firstSequence = true;

			while ((line = reader.readLine()) != null){
				String[] lineParts = line.split("\t");
				Integer customerID = Integer.valueOf(lineParts[0]);
				Integer orderNumber = Integer.valueOf(lineParts[1]);

				if(!customerID.equals(lastCustomerID)){
					if(firstSequence){
						firstSequence = false;
					}else{
						sequenceBuilder.append("-2");
						writerSequence.write(sequenceBuilder.toString());
						writerSequence.newLine();

						sequenceBuilder = new StringBuilder("");
					}

					lastCustomerID = customerID;
				} else if(orderNumber <= lastOrderNumber){
					System.out.println("Line "+ lineNumber +" :c");
				}

				String[] items = lineParts[3].split(",");
				for(String item : items){
					if(item.equals("")){
						break;
					}

					Integer itemID = mapNameId.getOrDefault(item, -1);

					if(itemID.equals(-1)){
						mapNameId.put(item, freeID);
						itemID = freeID;
						freeID++;
					}

					sequenceBuilder.append(itemID);
					sequenceBuilder.append(' ');
				}
				sequenceBuilder.append("-1 ");

				lastOrderNumber = orderNumber;
				lineNumber++;
			}

			for(String item : new TreeSet<>(mapNameId.keySet())){
				writerInfo.write(item);
				writerInfo.write(',');
				writerInfo.write(mapNameId.get(item).toString());
				writerInfo.newLine();
			}

			reader.close();
			writerSequence.close();
			writerInfo.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public static void sortSequences(String sequenceDataset){
		try{
			BufferedReader reader = new BufferedReader(new FileReader(sequenceDataset));
			StringBuilder stringBuilder = new StringBuilder("");
			String line;

			while ((line = reader.readLine()) != null){
				String[] linePart = line.split(" -1 ");

				for(String itemset : linePart) {
					if(!itemset.contains("-2")) {
						SortedSet<Integer> sortedSet = new TreeSet<>();
						String[] items = itemset.split(" ");

						for (String item : items) {
							sortedSet.add(Integer.valueOf(item));
						}

						for(Integer item_id : sortedSet){
							stringBuilder.append(item_id);
							stringBuilder.append(' ');
						}
					}

					stringBuilder.append("-1 ");
				}

				stringBuilder.delete(stringBuilder.lastIndexOf("-1"), stringBuilder.lastIndexOf("-1") +3);

				stringBuilder.append("-2\n");
			}

			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(sequenceDataset));

			writer.write(stringBuilder.toString());

			writer.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
