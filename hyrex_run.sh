START_DATE=$(date)
echo "$START_DATE started running the script ....." 

#-------------------------------------------------------------

source hyrex_sen_run.sh

export HyREX_DIR=/media/Study/workspace/HyREX

export DATA_HOME=$HyREX_DIR/sample-data

export SVM_LIGHT_TK=$HyREX_DIR/SVM-Light-TK-1.5

export OUT_DIR=$HyREX_DIR/out

export OUT_FILE=$OUT_DIR/output.txt
export PRED_FILE=$SVM_LIGHT_TK/svm_predictions


export TRACE_FILE=$OUT_DIR/trace

export LIB_DIR=$HyREX_DIR/lib

export TRAIN_DATA_FULL="-train  $DATA_HOME/sample.full"
export TRAIN_DATA_PARSED="-trainParse $DATA_HOME/sample.parsed.bllip.complete"

export HELDOUT_DATA_FULL="-test  $DATA_HOME/sample.full"
export HELDOUT_DATA_PARSED="-testParse $DATA_HOME/sample.parsed.bllip.complete"

export TEST_DATA_FULL="-test  $DATA_HOME/sample.full"
export TEST_DATA_PARSED="-testParse $DATA_HOME/sample.parsed.bllip.complete"


export CR="" #-icrTraini -icrTest"
export CB="" #-cbTrain $DATA_HOME/$CORP/edu_seg/$CORP.parsed.bllip.seg"

export CROSS_FOLD="" #-foldFilesFolder  $DATA_HOME/folds"

export JVM_ARGS="-Xmx1048m  -XX:MaxPermSize=512m -cp ./bin:$LIB_DIR/jsre.jar:$LIB_DIR/log4j-1.2.8.jar:$LIB_DIR/commons-digester.jar:$LIB_DIR/commons-collections.jar:$LIB_DIR/commons-logging.jar:$LIB_DIR/commons-beanutils.jar:$LIB_DIR/jutil.jar:$LIB_DIR/libsvm.jar:$LIB_DIR/bioRelEx.jar:$LIB_DIR/stanford-parser-2012-03-09-models.jar:$LIB_DIR/stanford-parser.jar"

rm -rf $OUT_DIR/*

START_DATE=$(date)
echo "$START_DATE started running the script ....." 

export m=1024

#****  NOTE  *****
# 
#  This script 
#
#********************

NO_OF_FOLDS=1
BEST_RES_FILE=$OUT_DIR/best_tuned_result_ddi_sen
All_PRED_FILE=$OUT_DIR/base.stat.in_ddi_sen

CROSS_FOLD="" #-foldFilesFolder  $DATA_HOME/folds"


#################################################
# Initialization of the kernel types to be used
#################################################

JSRE="-jsre"	# Possible value: "-jsre" (Giuliano, Lavelli and Romano, EACL 2006)
export KERNEL_JSRE="SL"	# Possible value: "SL", "LC" and "GC" -- all of them are vector based kernels (Giuliano, Lavelli and Romano, EACL 2006)


#################################################
# Initialization of SVM-Light-TK parameters
#################################################

# values of 't':
# 5 = 1 tree + 1 vector kernels
# 0 = 1 vector kernel (linear)
# 50 = 2 vector kernels
# 502 = 2 vector + 1 tree kernels
#

# set parameter values here
T=1.0
t=0
C="V" # + 
F="4"
cost=0.2
b=1
lambda=0.4
mu=0.4
d=2 # parameter d in polynomial kernel: (s a*b+c)^d
U=0

best_lambda=$lambda
best_mu=$mu
best_cost=$cost
best_d=$d

prev_f1=0.0
f1=1
cont=1
offset=1.0

max_iter_sub_cost=1
max_iter_sub_lambda=2
max_iter_add=25

param="-medtType 6    $TRAIN_DATA_PARSED   $TRAIN_DATA_FULL  $HELDOUT_DATA_PARSED  $HELDOUT_DATA_FULL  $CROSS_FOLD  $CR  $CB -nf $NO_OF_FOLDS -trigFile $HyREX_DIR/db/ddi_trigger -classifySentences"

echo $param

#####################################################
# Run HyREX to generate training and test instances for identifying less informative sentences using negation  (see Chowdhury & Lavelli, NAACL 2013)
#####################################################

cd $HyREX_DIR
java  $JVM_ARGS Kernels.TKOutputGenerator  $param


echo "" > $TRACE_FILE


export findNegatedSentence=1
fnExp


#==============================================================

NO_OF_FOLDS=1
export BEST_RES_FILE=$OUT_DIR/best_tuned_result
export All_PRED_FILE=$OUT_DIR/base.stat.in

#################################################
# Initialization of the kernel types to be used
#################################################

# Leave emtpty string for a parameter if none of the values are to be used

export DT="-wv" #-zhou2005" 	# Possible values: "-dt" and "-wv". 
		# "-dt" is for MEDT tree kernel compuation (Chowdhury, Lavelli and Moschitti, BioNLP 2011)
		# "-wv" is for TPWF vector-based kernel compuation (Chowdhury and Lavelli, EACL 2012)

export PST="-pst"	# Possible value: "-pst" for PET tree kernel computation (Moschitti, ACL 2004; Chowdhury and Lavelli, EACL 2012)

export JSRE="-jsre"	# Possible value: "-jsre" (Giuliano, Lavelli and Romano, EACL 2006)
export KERNEL_JSRE="SL"	# Possible value: "SL", "LC" and "GC" -- all of them are vector based kernels (Giuliano, Lavelli and Romano, EACL 2006)


#################################################
# Initialization of SVM-Light-TK parameters
#################################################

# values of 't':
# 5 = 1 tree + 1 vector kernels
# 0 = 1 vector kernel (linear)
# 50 = 2 vector kernels
# 502 = 2 vector + 1 tree kernels
#

# set parameter values here
T=1.0
t=502
C="+" # + 
F="4"
cost=0.2
b=1
lambda=0.4
mu=0.4
d=2 # parameter d in polynomial kernel: (s a*b+c)^d
U=0

best_lambda=$lambda
best_mu=$mu
best_cost=$cost
best_d=$d

prev_f1=0.0
f1=1
cont=1
offset=1.0

max_iter_sub_cost=1
max_iter_sub_lambda=2
max_iter_add=25


#####################################################
# Run HyREX to generate training and test instances for relation extraction
#####################################################

param="$PST $JSRE $DT -kjsre $KERNEL_JSRE -medtType 6    $TRAIN_DATA_PARSED   $TRAIN_DATA_FULL   $TEST_DATA_PARSED   $TEST_DATA_FULL  $CROSS_FOLD  $CR  $CB -nf $NO_OF_FOLDS -trigFile $HyREX_DIR/db/ddi_trigger" #-doNotPrepareData

echo $param

cd $HyREX_DIR
java $JVM_ARGS Kernels.TKOutputGenerator $param


export findNegatedSentence=0
fnExp


echo ""
echo "$(date) -> All processing are done. (Started at $START_DATE)"
echo ""
echo "Results of evaluation can be found in $OUT_DIR/output.txt"
echo ""

rm $OUT_DIR/entPairFileName_DT
rm $OUT_DIR/entPairFileName_JSRE
rm $OUT_DIR/entPairFileName_WV
rm $OUT_DIR/entPairFileName_PST
rm $OUT_DIR/*.parsed.*
rm $OUT_DIR/model
#rm $OUT_DIR/output.txt
#rm $OUT_DIR/trace
rm $OUT_DIR/base.stat.in
#rm $OUT_DIR/extracted_relations.txt
rm $OUT_DIR/all_vect_by_pair
rm -r $OUT_DIR/tk
rm $OUT_DIR/train.*
rm $OUT_DIR/test.*
rm $OUT_DIR/tpwf.*
rm $PRED_FILE
