
#####################################################
# Train and test using SVM-Light-TK-1.5
#####################################################


# Run the tool
fnRun()
{
	Parameters="-t $1 -C $2 -F $3  -c $4 -b $5 -L $6 -M $7 -m $8 -U $9 -T $T -V L -d $d"

	echo $Parameters >> $TRACE_FILE

	cd $SVM_LIGHT_TK
	declare -a tmp_file_list=($OUT_FILE  $PRED_FILE  $All_PRED_FILE $TRACE_FILE)
	for fn in "${tmp_file_list[@]}"
	do
	      fnDelFileOrDir $fn
	done

	pos=`grep "^1" $OUT_DIR/train.tk | wc -l`
	neg=`grep "^-1" $OUT_DIR/train.tk | wc -l`

	if [ $pos -lt 1 ]
  then
		echo "No of positive instance is zero. Aborting training."
		findNegatedSentence=0
		return
	fi

	if [ $pos -lt 1 ]
  then
		echo "No of negative instance is zero. Aborting training."
		findNegatedSentence=0
		return
	fi

	cf=`echo $neg / $pos | bc -l`

	echo "pos=$pos neg=$neg cf=$cf" >> $OUT_FILE

	if [ -n "$TRAIN_DATA_FULL" ]; then
		echo "$(date) -> Training started..."
		echo "$(date) -> Training started..." >> $TRACE_FILE
		$SVM_LIGHT_TK/svm_learn $Parameters -j $cf $OUT_DIR/train.tk $OUT_DIR/model
	fi

	echo "$(date) -> Testing started..."
	echo "$(date) -> Testing started..." >> $TRACE_FILE
	$SVM_LIGHT_TK/svm_classify $OUT_DIR/test.tk $OUT_DIR/model >> $OUT_FILE
	less $PRED_FILE >> $All_PRED_FILE

	echo ""  >> $OUT_FILE
	fnPrint $t $C $F $cost $b $lambda $mu $m $U
}


# Do n-fold cross validation
fnCrossFold()
{
	Parameters="-t $1 -C $2 -F $3  -c $4 -b $5 -L $6 -M $7 -m $8 -U $9 -T $T -V L -d $d"
	CORPUS_FILES_DIR=$OUT_DIR/tk

	echo $Parameters >> $TRACE_FILE

	declare -a tmp_file_list=($OUT_FILE  $PRED_FILE  $All_PRED_FILE)
	for fn in "${tmp_file_list[@]}"
	do
	      fnDelFileOrDir $fn
	done

	cd $SVM_LIGHT_TK

	for (( i=1; i<=$NO_OF_FOLDS; i++));do
		pos=`grep "^1" $CORPUS_FILES_DIR/train-203-$i.tk | wc -l`
	        neg=`grep "^-1" $CORPUS_FILES_DIR/train-203-$i.tk | wc -l`
        	cf=`echo $neg / $pos | bc -l`
	        echo "pos=$pos neg=$neg cf=$cf" >> $OUT_FILE

		echo "Learning on train fold $i"
		echo "$(date) -> training for fold $i" >> $TRACE_FILE
		$SVM_LIGHT_TK/svm_learn $Parameters -j $cf $CORPUS_FILES_DIR/train-203-$i.tk $OUT_DIR/model

		echo "Testing on test fold $i"
		echo "$(date) -> testing for fold $i" >> $TRACE_FILE
		$SVM_LIGHT_TK/svm_classify $CORPUS_FILES_DIR/test-203-$i.tk $OUT_DIR/model >> $OUT_FILE
		less $PRED_FILE >> $All_PRED_FILE

		echo ""  >> $OUT_FILE
	done

	echo "" >> $TRACE_FILE
	echo "----------------------------------------" >> $TRACE_FILE
	echo "" >> $TRACE_FILE
	echo "----------------------------------------"
	echo "----------------------------------------"
}


#####################################################
# Run HyREX to extract positive relations
#####################################################

fnExtractRels()
{
	cd $HyREX_DIR

	java $JVM_ARGS Others.PredictionResult $All_PRED_FILE  $OUT_DIR/entPairFileName_TK

	echo ""
	echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
	echo "$(date) -> Extracted relations are written in $OUT_DIR/extracted_relations.txt"
	echo "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%"
}

#####################################################



# Return F1 score
fnReturnF1()
{
	cd $HyREX_DIR

	f_score=`java $JVM_ARGS Kernels.TKOutputGenerator -ev tk -evalOutFile $OUT_FILE`

	echo $f_score
}


# Set the current F1 value as prev F1
fnSetPrevF1()
{
	echo $1
}



fnPrint() {
	less $OUT_FILE > $BEST_RES_FILE
	echo "Updating $BEST_RES_FILE file ...." >> $TRACE_FILE
	echo "-t $1 -C $2 -F $3  -c $4 -b $5 -L $6 -M $7 -m $8 -U $9 -T $T -d $d" >> $BEST_RES_FILE

	cd $HyREX_DIR

	BEST_All_PRED_FILE="$OUT_DIR/best.base.stat.in"
	less $All_PRED_FILE > $BEST_All_PRED_FILE

	java $JVM_ARGS Kernels.TKOutputGenerator $TRAIN_DATA_FULL -ev full -evalOutFile $OUT_FILE >>  $BEST_RES_FILE

	fnExtractRels
	#less $OUT_DIR/extracted_relations.txt > $OUT_DIR/best_extracted_relations.txt
}


# Optimize the c parameter by incrementing 0.5

fnOptimizePARAM() {

	change=$1
	echo "change=$change" >> $TRACE_FILE

	T=1.0
	offset=0.1

	best_lambda=$lambda
	best_mu=$mu
	best_cost=$cost

	init_lambda=$lambda
	init_mu=$mu
	init_cost=$cost

	for (( inc_var=2; inc_var<=max_iter_add; inc_var++ ))
	do
		if [ $change -gt 2 ]
                then
                	lambda=`echo $lambda + $offset | bc`
		elif [ $change -gt 1 ]
                then
                        mu=`echo $mu + $offset | bc`
                elif [ $change -gt 0 ]
                then
                        cost=`echo $cost + $offset | bc`
                fi

		fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
		f1=$(fnReturnF1)
		echo "f1=$f1 prev_f1=$prev_f1 offset=$offset" >> $TRACE_FILE

		echo "(1) fnOptimizePARAM() f1=$f1 prev_f1=$prev_f1 offset=$offset"
		if expr $f1 \> $prev_f1 >/dev/null ; then
			prev_f1=$(fnSetPrevF1 $f1)

			best_lambda=$(fnSetPrevF1 $lambda)
			best_mu=$(fnSetPrevF1 $mu)
			best_cost=$(fnSetPrevF1 $cost)

			fnPrint $t $C $F $cost $b $lambda $mu $m $U
                else
                        inc_var=1000
		fi
	done

	lambda=$init_lambda
	mu=$init_mu
	cost=$init_cost


	for (( inc_var=1; inc_var<=max_iter_sub; inc_var++ ))
	do

		if [ $change -gt 2 ]
    then
	      lambda=`echo $lambda - $offset | bc`
		elif [ $change -gt 1 ]
    then
        mu=`echo $mu - $offset | bc`
    elif [ $change -gt 0 ]
    then
      	cost=`echo $cost - $offset | bc`
    fi

		fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
		f1=$(fnReturnF1)
		echo "f1=$f1 prev_f1=$prev_f1 offset=$offset" >> $TRACE_FILE

		echo "(2) fnOptimizePARAM() f1=$f1 prev_f1=$prev_f1 offset=$offset"
		if expr $f1 \> $prev_f1 >/dev/null ; then
			prev_f1=$(fnSetPrevF1 $f1)

			best_lambda=$(fnSetPrevF1 $lambda)
			best_mu=$(fnSetPrevF1 $mu)
			best_cost=$(fnSetPrevF1 $cost)

			fnPrint $t $C $F $cost $b $lambda $mu $m $U
    else
      inc_var=1000
		fi
	done

	lambda=$best_lambda
	mu=$best_mu
	cost=$best_cost
}

#----------------------------------------------------------------------------------
# Optimize the T parameter by incrementing 0.1

fnOptimizeT() {

	at=0.1
	T=1.1
	for (( inc_var=1; inc_var<=25; inc_var++ ))
	do
		fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
		f1=$(fnReturnF1)
		echo "f1=$f1 prev_f1=$prev_f1 offset=$at" >> $TRACE_FILE

		echo "(1) fnOptimizeT() f1=$f1 prev_f1=$prev_f1 offset=$at"
		if expr $f1 \> $prev_f1 >/dev/null ; then
			prev_f1=$(fnSetPrevF1 $f1)
			fnPrint $t $C $F $cost $b $lambda $mu $m $U

			T=`echo $T + $at | bc`
                else
                        inc_var=1000
		fi
	done

	T=0.9
	for (( inc_var=1; inc_var<=8; inc_var++ ))
	do
		fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
		f1=$(fnReturnF1)
		echo "f1=$f1 prev_f1=$prev_f1 offset=$at" >> $TRACE_FILE

		echo "(2) fnOptimizeT() f1=$f1 prev_f1=$prev_f1 offset=$at"
		if expr $f1 \> $prev_f1 >/dev/null ; then
			prev_f1=$(fnSetPrevF1 $f1)
			fnPrint $t $C $F $cost $b $lambda $mu $m $U

			T=`echo $T - $at | bc`
                else
                        inc_var=1000
		fi
	done
}

#----------------------------------------------------------------------------------
# Optimize the d parameter by incrementing 1

fnOptimize_poly_d() {

	at=1
	best_d=2
	echo "f1=$f1 prev_f1=$prev_f1 offset=$at"
	for (( inc_var=2; inc_var<=9; inc_var++ ))
	do
		d=$inc_var
		fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
		f1=$(fnReturnF1)

		echo "f1=$f1 prev_f1=$prev_f1 offset=$at" >> $TRACE_FILE

		echo "(1) fnOptimize_poly_d() f1=$f1 prev_f1=$prev_f1 offset=$at"
		if expr $f1 \> $prev_f1 >/dev/null ; then
			prev_f1=$(fnSetPrevF1 $f1)
			fnPrint $t $C $F $cost $b $lambda $mu $m $U
			best_d=$d
    else
      inc_var=1000
		fi
	done

	d=$best_d
}
#-------------------------------

# Select starting point for COST and lambda parameter

fnSelectStartingCostValue() {

	#c=1.0
	#L=.5
	#M=0.4
	#T=1.1

	cost=0.2
	fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
	prev_f1=$(fnReturnF1)

	fnPrint $t $C $F $cost $b $lambda $mu $m $U

	cost=1.0
	f1=0
	#fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
	#f1=$(fnReturnF1)

	echo "f1=$f1 prev_f1=$prev_f1 " >> $TRACE_FILE

	echo "(1) fnSelectStartingCostValue() f1=$f1 prev_f1=$prev_f1"
	if expr $f1 \> $prev_f1 >/dev/null ; then
		prev_f1=$(fnSetPrevF1 $f1)
		max_iter_sub_cost=8
		fnPrint $t $C $F $cost $b $lambda $mu $m $U
	else
		cost=0.2
		max_iter_sub_cost=1
	fi

#	echo "F1 $prev_f1" >> $BEST_RES_FILE
}

fnSelectStartingLambdaValue() {

	lambda=1.0
	fnCrossFold $t $C $F $cost $b $lambda $mu $m $U $T
	f1=$(fnReturnF1)

	echo "(1) fnSelectStartingLambdaValue() f1=$f1 prev_f1=$prev_f1"
	if expr $f1 \> $prev_f1 >/dev/null ; then
		prev_f1=$(fnSetPrevF1 $f1)
		fnPrint $t $C $F $cost $b $lambda $mu $m $U
		max_iter_sub_lambda=8
	else
              	lambda=0.4
		max_iter_sub_lambda=2
	fi
}


fnExp() {
	if [ -n "$CROSS_FOLD" ]; then
		if (( t=50 )); then
			echo "t=50"
			fnOptimize_poly_d
		fi

		fnSelectStartingCostValue


		if [ -n "$PST" ]; then 	# -n tests to see if the argument is non empty
			# "the variable X is not the empty string"

		#	fnSelectStartingLambdaValue

			# Optimize the lambda parameter
			optL=3
			max_iter_sub=$max_iter_sub_lambda

			fnOptimizePARAM $optL
			echo "lambda $lambda" >> $TRACE_FILE

			max_iter_sub=2
			# Optimize the mu parameter
			optM=2
			fnOptimizePARAM $optM
			echo "mu $mu" >> $TRACE_FILE
		fi


		# Optimize the c parameter
		optC=1
		max_iter_sub=$max_iter_sub_cost

		fnOptimizePARAM $optC
		echo "cost $cost" >> $TRACE_FILE

		if [ -n "$PST" ]; then 	# -n tests to see if the argument is non empty
			fnOptimizeT
		fi
	else
		fnRun  $t $C $F $cost $b $lambda $mu $m $U $T

		if [ $findNegatedSentence -gt 0 ]
                then
			cd $HyREX_DIR
			java $JVM_ARGS  negation.NegatedSentenceAnalyser  $BEST_All_PRED_FILE  out/allSenIdsForTest  neg
		fi
	fi
}
