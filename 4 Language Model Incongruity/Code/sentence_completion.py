import numpy
import six
import sys
import traceback
import re
import math
from chainer import cuda
from context2vec.common.context_models import Toks
from context2vec.common.model_reader import ModelReader
import cPickle
import re


from sklearn.metrics.pairwise import cosine_similarity;
import warnings
warnings.filterwarnings('ignore')

from nltk.corpus import wordnet as wn


l1 = []
from sklearn.metrics.pairwise import cosine_similarity;
import warnings
warnings.filterwarnings('ignore')

import pickle
with open('/data/aa1/PhD_Sem8/AAAI17Demo/glove.840B.300d.pkl_filtered.ver2','rb') as infile:
	wv_model = pickle.load(infile);



f = open ('/data/aa1/PhD_Sem8/TheLastLeg/stopwords2.kk','r')
for line in f:
	l1.append(line.strip())


class ParseException(Exception):
    def __init__(self, str):
        super(ParseException, self).__init__(str)

target_exp = re.compile('\[.*\]')

def parse_input(line):
	sent = line.strip().split()
	target_pos = None
	for i, word in enumerate(sent):
		if target_exp.match(word) != None:
			target_pos = i
			if word == '[]':
				word = None
			else:
				word = word[1:-1]
			sent[i] = word
	return sent, target_pos


def mult_sim(w, target_v, context_v):
	target_similarity = w.dot(target_v)
	target_similarity[target_similarity<0] = 0.0
 	context_similarity = w.dot(context_v)
	context_similarity[context_similarity<0] = 0.0
	return (target_similarity * context_similarity)


if len(sys.argv) < 2:
	print >> sys.stderr, "Usage: %s <model-param-file>"  % (sys.argv[0])
	sys.exit(1)

model_param_file = sys.argv[1]
n_result = 50  # number of search result to show
gpu = -1 # todo: make this work with gpu


if gpu >= 0:
    cuda.check_cuda_available()
    cuda.get_device(gpu).use()
xp = cuda.cupy if gpu >= 0 else numpy

model_reader = ModelReader(model_param_file)
w = model_reader.w
word2index = model_reader.word2index
index2word = model_reader.index2word
model = model_reader.model


f2 = open(sys.argv[2], 'r')
sep = sys.argv[3]
f_test = open(sys.argv[4],'r')
f_testo = open(sys.argv[4]+'.sentencecompl','w')
if sep == 'p':
	sep = '\t'

sarc_vals = []
linenum = 0
nonsarc_vals = []
for line in f2:
	linenum += 1
	if linenum % 10 == 0:
		print('..'+ str(linenum))
	hm_val = {}
	max_log_w = 0.0
	f3 = line.split(sep)
	if len(f3) != 2:
		continue

	text = f3[0].strip()
	value = 1.0
 	label = f3[1].strip().lower()
	text = text.lower()
	words = re.findall(r"[\w']+|[.,!?;]", text)

	for ipp in range(0,len(words)):
		tt_line = ''
		if words[ipp].isalnum() and words[ipp] not in l1:
			orig_word = words[ipp]
			if orig_word not in wv_model.keys():
				continue 
			left = ''
			right = ''

			for j in range(0,ipp):
				left += words[j]+' '

			for j in range(ipp+1,len(words)):
				right += words[j]+' '
			tt_line = left + ' [] ' + right+'.';
			sent, target_pos = parse_input(tt_line)
			if target_pos == None:
				continue
			if sent[target_pos] == None:
				target_v = None
			elif sent[target_pos] not in word2index:
				continue
			else:
				target_v = w[word2index[sent[target_pos]]]

			if len(sent) > 1:
				context_v = model.context2vec(sent, target_pos)
				context_v = context_v / xp.sqrt((context_v * context_v).sum())
			else:
				context_v = None
			

                        if target_v is not None and context_v is not None:
                                similarity = mult_sim(w, target_v, context_v)
                        else:
                                if target_v is not None:
                                        v = target_v
                                elif context_v is not None:
                                        v = context_v
                                else:
                                        continue
                                similarity = (w.dot(v)+1.0)/2 # Cosine similarity can be negative, mapping      imilarity to [0,1]
                        count = 1
                        best_word = ''
                        best_similarity = ''
                        displayed = 0

			for i in (-similarity).argsort():
				if numpy.isnan(similarity[i]):
					continue
                                #print('{0}: {1}'.format(index2word[i], similarity[i]))
                                if str(index2word[i]) in wv_model.keys() and str(orig_word) in wv_model.keys():
					new_value = cosine_similarity(wv_model[index2word[i]], wv_model[orig_word])[0][0]
					new_value = (new_value + 1.0)/2
					if new_value < value:
						value = new_value
					# print(tweet_id+'\t'+label+'\t'+orig_word+'\t'+index2word[i]+'\t'+str(get_similarity(orig_word,index2word[i])))
                                count += 1
                                if count == n_result:
                                        break

				#if numpy.isnan(similarity[i]):
                                #        continue
                                #if (index2word[i] == orig_word):
                                #        displayed = 1
                                #        if math.log(count) > max_log_w:
                                #                max_log_w = math.log(count)
                                #count += 1
                                #if count == n_result and displayed == 0:
                                #        count = -1

	if label == 'sarcasm' and float(value) != 1.0:
		print(str(value) + ' value hai aur main ise sarc vals mein daal raa')
		sarc_vals.append(value)
	elif float(value) != 1.0:
		nonsarc_vals.append(value)
		print(str(value) + ' value hai aur main nonsarc vals mein daal ra')
print('now estimating .... ')
min_sarc = 1
min_nsarc = 1

if (len(sarc_vals)) != 0:
	min_sarc = min(sarc_vals)
if(len(nonsarc_vals)) != 0:
	min_nsarc = min(nonsarc_vals)
print(str(min_sarc)+' '+ str(min_nsarc))
least = 0.0
if min_sarc < min_nsarc:
	least = min_sarc
else:
	least = min_sarc
max_sarc = 1
max_nsarc = 1

if len(sarc_vals) != 0:
	max_sarc = max(sarc_vals)

if len(nonsarc_vals) != 0:
	max_nsarc = max(nonsarc_vals)
print(str(max_sarc)+' '+str(max_nsarc))
most = 0.0
best_threshold = least
best_accuracy = 0.0
if max_sarc > max_nsarc:
	most = max_sarc
else:
	most = max_nsarc

curr_threshold = least
error = 0
while curr_threshold < most:
	correct = 0
	for i in range(0,len(sarc_vals)):
		if i == 0:
			print(str(sarc_vals[i]) + ' is being compared against ' + str(curr_threshold))
		if sarc_vals[i] < curr_threshold:
			correct += 1

	for i in range(0,len(nonsarc_vals)):
		if i == 0:
			print(str(nonsarc_vals[i]) + ' is being compared against '+str(curr_threshold) + ' non sarc')

		if nonsarc_vals[i] > curr_threshold:
			correct += 1	

	accuracy = float(correct) / (len(sarc_vals) + len(nonsarc_vals))
	print ('Accuracy is ' + str(accuracy) + ': ' + str(correct) + ' : ' + str(len(sarc_vals) + len(nonsarc_vals)))
	if accuracy > best_accuracy:
		best_accuracy = accuracy
		best_threshold = curr_threshold
	curr_threshold += 0.001

print(str(len(sarc_vals))+' '+str(len(nonsarc_vals))+' hagla re')
print(str(best_threshold) +' is the best value of threshold while '+ str(least)+ ' was the least')


tp = 0
tn = 0
fp = 0
fn = 0
position = 0
for line in f_test:
	print(' i want to die. ' + line)
	hm_val = {}
	if position % 10 == 0:
		print('...' + str(position))
	position += 1
	max_log_w = 0.0
	f3 = line.split(sep)
	if (len(f3)) != 2:
		f_testo.write('-1')
		continue	
	ftext = f3[0].strip()
	value = 1.0
	print('value was set to '+ str(value))
 	label = f3[1].strip().lower()
	text = ftext.lower()
	words = re.findall(r"[\w']+|[.,!?;]", text)
	print(' now i will test for the sentence '+ text)
	for ipp in range(0,len(words)):
		tt_line = ''
		if words[ipp].isalnum() and words[ipp] not in l1:
			orig_word = words[ipp]
			left = ''
			right = ''

			for j in range(0,ipp):
				left += words[j]+' '

			for j in range(ipp+1,len(words)):
				right += words[j]+' '
			tt_line = left + ' [] ' + right+'.';
			sent, target_pos = parse_input(tt_line)
			if target_pos == None:
				continue
			if sent[target_pos] == None:
				target_v = None
			elif sent[target_pos] not in word2index:
				continue
			else:
				target_v = w[word2index[sent[target_pos]]]

			if len(sent) > 1:
				context_v = model.context2vec(sent, target_pos)
				context_v = context_v / xp.sqrt((context_v * context_v).sum())
			else:
				context_v = None
			

                        if target_v is not None and context_v is not None:
                                similarity = mult_sim(w, target_v, context_v)
                        else:
                                if target_v is not None:
                                        v = target_v
                                elif context_v is not None:
                                        v = context_v
                                else:
                                        continue
                                similarity = (w.dot(v)+1.0)/float(2) # Cosine similarity can be negative, mapping      imilarity to [0,1]
                        count = 1
                        best_word = ''
                        best_similarity = ''
                        displayed = 0

			for i in (-similarity).argsort():
				if numpy.isnan(similarity[i]):
					continue
                                #print('{0}: {1}'.format(index2word[i], similarity[i]))
                                if str(index2word[i]) in wv_model.keys() and str(orig_word) in wv_model.keys():
					new_value = cosine_similarity(wv_model[index2word[i]], wv_model[orig_word])[0][0]
					new_value = (new_value + 1.0)/float(2)
					if new_value < value:
						value = new_value
					# print(tweet_id+'\t'+label+'\t'+orig_word+'\t'+index2word[i]+'\t'+str(get_similarity(orig_word,index2word[i])))
                                count += 1
                                if count == n_result:
                                        break

				#if numpy.isnan(similarity[i]):
                                #        continue
                                #if (index2word[i] == orig_word):
                                #        displayed = 1
                                #        if math.log(count) > max_log_w:
                                #                max_log_w = math.log(count)
                                #count += 1
                                #if count == n_result and displayed == 0:
                                #        count = -1

	print('so ... for this example, '+str(value)+' and ' + str(best_threshold) + ' were compared, ok?')
	if value < best_threshold:
		f_testo.write('+1'+'\n')
		print(label+' was predicted as sarcastic, ok?')
		if label == 'sarcasm':
			tp += 1
		else:
			fp += 1
	else:
		f_testo.write('-1'+'\n')
		print(label+' was predicted as non-sarcastic, ok?')
		if label != 'sarcasm':
			tn += 1
		else:
			fn += 1


f_testo.write(str(tp)+' '+str(fp)+' '+str(tn)+' '+str(fn))
print (str(tp)+' '+str(fp)+' '+str(tn)+' '+str(fn))
f_testo.close()
