package algorithm;

import ca.pfv.spmf.algorithms.frequentpatterns.apriori.AlgoApriori;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import main.DataExplorer;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Apriori {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Start Time: "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
		String experimentProduct = "Apriori-ProductID";
		String experimentAisle = "Apriori-AisleID";

		runApriori(DataExplorer.transactionsetAisle, DataExplorer.aprioriPatterns, 300d/131209);

		runExperimentApriori(DataExplorer.transactionsetProduct, experimentProduct);
		runExperimentApriori(DataExplorer.transactionsetAisle, experimentAisle);

		System.out.println("Apriori Ended.");
	}

	private static void runApriori(String transactionPath, String patternPath, double minsup){

		AlgoApriori apriori = new AlgoApriori();
		try {
			apriori.runAlgorithm(minsup,
					transactionPath,
					patternPath);
			apriori.printStats();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runExperimentApriori(String transactionPath, String experimentName){
		List<Integer> listMinsup = new ArrayList<>();
		int step = 1;

		for(int minsup = 300; minsup < 1000; minsup += 50){
			listMinsup.add(minsup);
		}

		Collections.shuffle(listMinsup);

		try {
			BufferedWriter writerTime = new BufferedWriter(new FileWriter(DataExplorer.workingDirectory + experimentName +"Time.perf"));
			BufferedWriter writerCount = new BufferedWriter(new FileWriter(DataExplorer.workingDirectory + experimentName +"Count.perf"));

			writerTime.write("minsup,time,algorithm");
			writerTime.newLine();

			writerCount.write("minsup,pattern_count,algorithm");
			writerCount.newLine();

			for(Integer minsup : listMinsup) {
				AlgoApriori apriori = new AlgoApriori();

				System.out.println("["+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) +"]Step "+ step +"/"+ listMinsup.size() +" - Starting with minsup: "+ minsup);

				long start = System.currentTimeMillis();
				Itemsets result = apriori.runAlgorithm(minsup/131209d, transactionPath, null);
				long end = System.currentTimeMillis();

				List<Itemset> lvl = new ArrayList<>();
				for(List<Itemset> level : result.getLevels()){
					lvl.addAll(level);
				}

				writerCount.write(minsup +","+ lvl.size() +","+ experimentName);
				writerCount.newLine();

				writerTime.write(minsup +","+ (end - start)/1000 +","+ experimentName);
				writerTime.newLine();

				step++;
			}

			writerTime.close();
			writerCount.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
