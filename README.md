HyREX
=====

This software is distributed under Apache License 2.0.

Modification, redistribution or any other usage of this software is permitted without any restriction, given that the following paper is cited:

  Md. Faisal Mahbub Chowdhury and Alberto Lavelli, "Exploiting Scopes of Negations and Heterogeneous Features for Relation Extraction: A Case Study for Drug-Drug Interaction Extraction", NAACL 2013

All the tools and libraries (e.g. SVM-Light-TK and the jar files under "lib" directory) used by this software have their own license type. Please check them if you intend to use them for commerical usage.

To run this software, please download the following jar files and place them under "lib" directory -

1. lvg2012api.jar
2. lvg2012dist.jar

(See here - http://lexsrv2.nlm.nih.gov/LexSysGroup/Projects/lvg/2012/web/index.html)

3. stanford-parser-2012-03-09-models.jar
4. stanford-parser.jar

(See here - http://nlp.stanford.edu/downloads/lex-parser.shtml)

Also, please compile the classes under "SVM-Light-TK-1.5" directory.

To run it, open hyrex_run.sh file and set the value of "HyREX_DIR" variable with the full path where this software is placed.
Open terminal and run open hyrex_run.sh.

(Make sure hyrex_sen_run.sh and hyrex_run.sh have execution and write permission.)


-- Faisal

