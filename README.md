# DMVHomework - Note to the teacher

Three data mining algorithms have been used for this homework: Apriori, LCM and BIDE+.  
Those three algorithms have each a separate class which are located in the package *algorithm* with their respective name.  
Each class has a main method which is able to launch either one instance of the selected algorithm or an experiment on this algorithm.  

In order to give inputs to those algorithms, the class *main.DatasetConverter* is able to take the raw dataset and format it in the good format for Apriori and LCM (*.transaction*) and for BIDE+ (*.sequence*).  
For BIDE+, since the raw dataset contains named items, the formatted *.sequence* will have only numbers in it. A file with the same name and an extension *.seqinfo* gives the association between the name of the item and its ID.

To explore the patterns returned by those algorithms, several methods in the class *main.DataExplorer* helps to choose meaningful patterns.  
