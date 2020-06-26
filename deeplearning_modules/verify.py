from networks.encoder_net import *
from networks.detecter_net import *
import torch
from torch.utils.data import DataLoader
from torchvision import datasets
import numpy as np
import pandas as pd
from PIL import Image, ImageEnhance
import time
# from tqdm import tqdm
from tqdm.auto import tqdm, trange
import seaborn as sns
import matplotlib.pyplot as plt
from eval import *
from torchvision import datasets, transforms
import scipy.io as sio
import scikitplot as skplt
import matplotlib.pyplot as plt
import pickle
import os
import sys
import warnings
import cv2


pre_align = False
dist_mode = 'cosine'  # 'l2' or 'cosine'
adjust_std = 0.1

if not sys.warnoptions:
    warnings.simplefilter("ignore")
# print(os.getcwd())
workers = 0 if os.name == 'nt' else 4
# workers = 0
device = torch.device('cuda:4' if torch.cuda.is_available() else 'cpu')
print('Running on device: {}'.format(device))

if pre_align:
    detecter = detecter_net(
        image_size=160, margin=0, min_face_size=20,
        thresholds=[0.6, 0.7, 0.7], factor=0.709, post_process=True,
        device=device
    )


    dataset = datasets.ImageFolder('../datasets/lfw')
    dataset.idx_to_class = {i:c for c, i in dataset.class_to_idx.items()}
    loader = DataLoader(dataset, collate_fn=collate_fn, batch_size=1, num_workers=workers)

    aligned = []
    names = []
    aligned_contrast = []
    aligned_brightness = []
    aligned_balance = []
    aligned_downsampled = []
    aligned_shifted = []

    for x, y in tqdm(loader, desc='Detecting faces'):

        x_aligned, prob = detecter(x, return_prob=True)
        x_aligned_contrast = detecter(ImageEnhance.Contrast(x).enhance(np.random.normal(1, adjust_std)), return_prob=False)
        x_aligned_brightness = detecter(ImageEnhance.Brightness(x).enhance(np.random.normal(1, adjust_std)), return_prob=False)
        x_aligned_balance = detecter(ImageEnhance.Color(x).enhance(np.random.normal(1, adjust_std)), return_prob=False)
        x_aligned_downsampled = detecter(cv2.resize(cv2.resize(np.asarray(x), (int(np.shape(x)[0] / 2), int(np.shape(x)[0] / 2)), interpolation=cv2.INTER_NEAREST), (int(np.shape(x)[0]), int(np.shape(x)[0])), interpolation=cv2.INTER_NEAREST), return_prob=False)
        x_aligned_shifted = detecter(shift_img(np.asarray(x), 25, 25), return_prob=False)

        if x_aligned is not None and x_aligned_balance is not None and x_aligned_brightness is not None and x_aligned_contrast is not None:
            # print('Face detected with probability: {:8f}'.format(prob))
            aligned.append(x_aligned)
            names.append(dataset.idx_to_class[y])
            aligned_balance.append(x_aligned_balance)
            aligned_brightness.append(x_aligned_brightness)
            aligned_contrast.append(x_aligned_contrast)
            aligned_downsampled.append(x_aligned_downsampled)
            aligned_shifted.append(x_aligned_shifted)

    with open('../datasets/lfw_cropped_list_new/aligned.pickle', 'wb') as fp:
        pickle.dump(aligned, fp)

    with open('../datasets/lfw_cropped_list_new/names.pickle', 'wb') as fp:
        pickle.dump(names, fp)

    with open('../datasets/lfw_cropped_list_new/align_contrast.pickle', 'wb') as fp:
        pickle.dump(aligned_contrast, fp)

    with open('../datasets/lfw_cropped_list_new/align_brightness.pickle', 'wb') as fp:
        pickle.dump(aligned_brightness, fp)

    with open('../datasets/lfw_cropped_list_new/align_balance.pickle', 'wb') as fp:
        pickle.dump(aligned_balance, fp)

    with open('../datasets/lfw_cropped_list_new/align_downsampled.pickle', 'wb') as fp:
        pickle.dump(aligned_downsampled, fp)

    with open('../datasets/lfw_cropped_list_new/align_shifted.pickle', 'wb') as fp:
        pickle.dump(aligned_shifted, fp)



else:
    with open ('../datasets/lfw_cropped_list_new/aligned.pickle', 'rb') as fp:
        aligned = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/names.pickle', 'rb') as fp:
        names = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/align_contrast.pickle', 'rb') as fp:
        aligned_contrast = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/align_brightness.pickle', 'rb') as fp:
        aligned_brightness = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/align_balance.pickle', 'rb') as fp:
        aligned_balance = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/align_downsampled.pickle', 'rb') as fp:
        aligned_downsampled = pickle.load(fp)

    with open('../datasets/lfw_cropped_list_new/align_shifted.pickle', 'rb') as fp:
        aligned_shifted = pickle.load(fp)


# exit()
encoder = encoder_net(pretrained='vggface2').eval().to(device)
batch_size = 200
num_batch = len(aligned) // batch_size
features = torch.zeros(0, 512)
features_balance = torch.zeros(0, 512)
features_brightness = torch.zeros(0, 512)
features_contrast = torch.zeros(0, 512)
features_downsampled = torch.zeros(0, 512)
features_shifted = torch.zeros(0, 512)

for i in tqdm(range(num_batch), desc='Calculating features'):
    # aligned_batch = torch.stack(aligned)[i*batch_size:(i+1)*batch_size, ...].to(device)


    features = torch.cat((features, encoder(torch.stack(aligned)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    features_balance = torch.cat((features_balance, encoder(torch.stack(aligned_balance)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    features_contrast = torch.cat((features_contrast, encoder(torch.stack(aligned_contrast)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    features_brightness = torch.cat((features_brightness, encoder(torch.stack(aligned_brightness)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    features_downsampled = torch.cat((features_downsampled, encoder(torch.stack(aligned_downsampled)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    features_shifted = torch.cat((features_shifted, encoder(torch.stack(aligned_shifted)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)
    # print()

# exit()
same_person_dist = []
different_person_dist = []
for i in tqdm(range(0, len(features) - 1), desc='Calculating distances of same/different person'):
    for j in range(i + 1, len(features)):
        dist = tensor_dist(features[i], features[j], mode=dist_mode)
        if names[i] == names[j]:
            same_person_dist.append(dist)
            # print(names[i] + ' ' + names[j] + ': ' + str(dist))

        else:
            different_person_dist.append(dist)
        # print(names[i] + ' ' + names[j] + ': ' + str(dist))
        # exit()

contrast_dist = []
brightness_dist = []
balance_dist = []
downsampled_dist = []
shifted_dist = []
for i in tqdm(range(0, len(features) - 1), desc='Calculating distances of adjusted face'):
    contrast_dist.append(tensor_dist(features[i], features_contrast[i], mode=dist_mode))
    brightness_dist.append(tensor_dist(features[i], features_brightness[i], mode=dist_mode))
    balance_dist.append(tensor_dist(features[i], features_balance[i], mode=dist_mode))
    downsampled_dist.append(tensor_dist(features[i], features_downsampled[i], mode=dist_mode))
    shifted_dist.append(tensor_dist(features[i], features_shifted[i], mode=dist_mode))


# print(contrast_dist)
# print(same_person_dist)
print('same pair '+str(np.shape(same_person_dist)))
print('different pair '+str(np.shape(different_person_dist)))
sio.savemat('results/same_person_'+dist_mode+'_dist.mat', {'same_person_dist': same_person_dist})
sio.savemat('results/different_person_'+dist_mode+'_dist.mat', {'different_person_dist': different_person_dist})
sio.savemat('results/contrast_'+dist_mode+'_dist.mat', {'contrast_dist': contrast_dist})
sio.savemat('results/balance_'+dist_mode+'_dist.mat', {'balance_dist': balance_dist})
sio.savemat('results/brightness_'+dist_mode+'_dist.mat', {'brightness_dist': brightness_dist})
sio.savemat('results/downsampled_'+dist_mode+'_dist.mat', {'downsampled_dist': downsampled_dist})
sio.savemat('results/shifted_'+dist_mode+'_dist.mat', {'shifted_dist': shifted_dist})




distances = []
labels = []
distances.append(same_person_dist)
distances.append(different_person_dist)
labels.append(np.ones(np.shape(same_person_dist)))
labels.append(np.zeros(np.shape(different_person_dist)))
labels = np.array([sublabel for label in labels for sublabel in label])
distances = np.array([subdist for dist in distances for subdist in dist])
sio.savemat('results/'+dist_mode+'_dists.mat', {'dists': distances})
sio.savemat('results/'+dist_mode+'_labels.mat', {'labels': labels})


# threshold_list = [1, 1.1, 1.2, 1.3]
# for threshold in threshold_list:
#     print()
#     true_positive_num = 0
#     true_negative_num = 0
#     false_positive_num = 0
#     false_negative_num = 0
#     for i in range(len(distances)):
#         # true positive
#         if labels[i] == 1 and distances[i] <= threshold:
#             true_positive_num += 1
#
#         # true negative
#         elif labels[i] == 1 and distances[i] > threshold:
#             true_negative_num += 1
#
#         # false positive
#         elif labels[i] == 0 and distances[i] <= threshold:
#             false_positive_num += 1
#
#         # false negative
#         elif labels[i] == 0 and distances[i] > threshold:
#             false_negative_num += 1
#
#     total_true_num = (true_positive_num + true_negative_num)
#     total_false_num = (false_positive_num + false_negative_num)
#
#     true_positive_rate = true_positive_num / total_true_num
#     true_negative_rate = true_negative_num / total_true_num
#     false_positive_rate = false_positive_num / total_false_num
#     false_negative_rate = false_negative_num / total_false_num
#     print('threshold '+ str(threshold)+'. '+'true_positive_rate: ' + str(true_positive_rate))
#     print('threshold '+ str(threshold)+'. '+'true_negative_rate: ' + str(true_negative_rate))
#     print('threshold '+ str(threshold)+'. '+'false_positive_rate: ' + str(false_positive_rate))
#     print('threshold '+ str(threshold)+'. '+'false_negative_rate: ' + str(false_negative_rate))
