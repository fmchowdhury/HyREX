HyREX
=====

**`HyREX`** stands for **Hybrid Relation Extractor**. 
It is a multi-phase hybrid kernel based approach for supervised relation extraciton that -

- combines a feature based kernel (Chowdhury and Lavelli, *EACL 2012*), Shallow Linguistic (SL)
kernel (Giuliano et al., *EACL 2006*), Path-enclosed Tree (PET) kernel (Moschitti, *ACL 2004*)
- exploits scope of negations (Chowdhury and Lavelli, *NAACL 2013*)
- discards uninformative training instances using semantic roles and contextual evidence (Chowdhury and Lavelli, *COLING 2012*)


This software is distributed under Apache License 2.0.

Modification, redistribution or any other usage of this software is permitted without any restriction, given that the following papers are cited:

```
[1] Md. Faisal Mahbub Chowdhury and Alberto Lavelli, "Exploiting Scopes of Negations and Heterogeneous Features for Relation Extraction: A Case Study for Drug-Drug Interaction Extraction", NAACL 2013


[2] Md Faisal Mahbub Chowdhury, Alberto Lavelli, "FBK-irst: A Multi-Phase Kernel Based Approach for Drug-Drug Interaction Detection and Classification that Exploits Linguistic Information", SemEval 2013
```


### Disclaimer

All the 3rd party tools and libraries (e.g. SVM-Light-TK and the jar files under "lib" directory) used by this software have their own license type. Please check them if you intend to use them for commerical usage.

### Required Libraries/jars

To run this software, please download the following jar files and place them under "lib" directory -

1. lvg2012api.jar
2. lvg2012dist.jar

(Download `lvg2012.tgz` from https://lexsrv3.nlm.nih.gov/LexSysGroup/Projects/lvg/2012/release/lvg2012.tgz , unzip it, go inside the `lib` folder inside the unzipped folder and you will find those 2 jars.)

3. stanford-parser-2012-03-09-models.jar
4. stanford-parser.jar

(Download `stanford-parser-2012-03-09.tgz` from https://nlp.stanford.edu/software/stanford-parser-2012-03-09.tgz , unzip it, the above 2 jars are inside the unzipped folder.)

### Data to train the system

There are 2 sample files inside `sample-data` folder which shows format for the input data.

To replicate the results of the SemEval-2013 DDI Extraction task, obtain annotated training data from https://www.cs.york.ac.uk/semeval-2013/task9/index.php%3Fid=data.html . Please convert them into the format of the sample files.

### How to run the system

To run it, open terminal, `cd` to the `HypRex` folder and run the following command -

`sh hyrex_run.sh`

(Make sure hyrex_sen_run.sh and hyrex_run.sh have execution and write permission.)


-- Faisal

