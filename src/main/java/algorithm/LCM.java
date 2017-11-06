package algorithm;

import ca.pfv.spmf.algorithms.frequentpatterns.lcm.AlgoLCM;
import ca.pfv.spmf.algorithms.frequentpatterns.lcm.Dataset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemset;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import main.DataExplorer;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LCM {

	public static void main(String[] args) {
		System.out.println("Start Time: "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
		String exprimentProduct = "LCM-ProductID";

		runLCM(DataExplorer.transactionsetProduct, DataExplorer.lcmPatterns, 300d/131209);

		runExperimentLCM(DataExplorer.transactionsetProduct, exprimentProduct);

		System.out.println("LCM Ended.");
	}

	private static void runLCM(String datasetPath, @Nullable String output, double minsup){

		AlgoLCM lcm = new AlgoLCM();
		try {
			Dataset dataset = new Dataset(datasetPath);
			lcm.runAlgorithm(minsup, dataset, output);
			lcm.printStats();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void runExperimentLCM(String transactionPath, String experimentName){
		List<Integer> listMinsup = new ArrayList<>();
		int step = 1;

		for(int minsup = 50; minsup < 1000; minsup += 50){
			listMinsup.add(minsup);
		}

		Collections.shuffle(listMinsup);

		try {
			BufferedWriter writerTime = new BufferedWriter(new FileWriter(DataExplorer.workingDirectory + "performanceLCMTime.csv"));
			BufferedWriter writerCount = new BufferedWriter(new FileWriter(DataExplorer.workingDirectory + "performanceLCMCount.csv"));

			writerTime.write("minsup,time,algorithm");
			writerTime.newLine();

			writerCount.write("minsup,pattern_count,algorithm");
			writerCount.newLine();

			for(Integer minsup : listMinsup) {
				AlgoLCM lcm = new AlgoLCM();
				// if true in next line it will find only closed itemsets, otherwise, all frequent itemsets

				System.out.println("["+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) +"]Step "+ step +"/"+ listMinsup.size() +" - Starting with minsup: "+ minsup);
				long start = System.currentTimeMillis();
				Dataset dataset = new Dataset(transactionPath);
				Itemsets itemsets = lcm.runAlgorithm(minsup/131209d, dataset, null);
				long end = System.currentTimeMillis();


				List<Itemset> lvl = new ArrayList<>();
				for(List<Itemset> level : itemsets.getLevels()){
					lvl.addAll(level);
				}

				writerCount.write(minsup +","+ lvl.size() +",LCM-ProductId");
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
