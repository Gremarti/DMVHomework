package algorithm;

import ca.pfv.spmf.algorithms.sequentialpatterns.prefixspan.AlgoBIDEPlus;
import main.DataExplorer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BIDEPlus {

	public static void main(String[] args) {
		System.out.println("Start Time: "+ LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));

		runBIDEPlus(DataExplorer.transactionsetSequence, DataExplorer.bideplusPatterns);
	}

	private static void runBIDEPlus(String sequenceDataset, String patternOutput){
		try {
			AlgoBIDEPlus bideplus = new AlgoBIDEPlus();
			double minsup = 400d/19999;

			bideplus.setShowSequenceIdentifiers(false);
			bideplus.runAlgorithm(sequenceDataset, minsup, patternOutput);

			bideplus.printStatistics();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
