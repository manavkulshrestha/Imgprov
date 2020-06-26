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


# set cuda device
if not sys.warnoptions:
    warnings.simplefilter("ignore")
device = torch.device('cuda:7' if torch.cuda.is_available() else 'cpu')
print('Running on device: {}'.format(device))

# initialize detecter_net for face detection
detecter_net = detecter_net(image_size=160, margin=0, min_face_size=20, thresholds=[0.6, 0.7, 0.7],
                            factor=0.709, post_process=True, device=device)

# initialize encoder_net for feature vector generation
encoder = encoder_net(pretrained='vggface2').eval().to(device)
# torch.save(resnet.state_dict(), os.path.join('models/vgg_new.pt'))
# exit()

# generate distances between faces of same person and different people, respectively
aligned_unadjusted = preprocess_test_imgs(detecter_net, mode='unadjusted', dataset_path='../datasets/lfw', restore=False)
aligned_names = preprocess_test_imgs(detecter_net, mode='names', dataset_path='../datasets/lfw', restore=False)
features = get_features(aligned_unadjusted, encoder, device, batch_size=200, tqdm_name='unadjusted', restore=False)
exit()
same_person_dist, different_person_dist = get_dist_same_different(features, aligned_names, restore=False)


# generate distances between truth and its adjusted selves
def gen_dist_from_adjusted_dataset(detecter_net, resnet, adjust_method, features_original, device):
    aligned = preprocess_test_imgs(detecter_net, mode=adjust_method, dataset_path='../datasets/lfw', restore=False)
    features_adjusted = get_features(aligned, resnet, device, batch_size=200, tqdm_name=adjust_method)
    dist = get_dist(features_original, features_adjusted, name=adjust_method, restore=False)
    return dist


# generate distances between truth and its various adjusted versions
adjust_method_list = ['brightness', 'balance', 'contrast', 'downsampled', 'shifted']
dist_list = [features]
for adjust_method in adjust_method_list:
    dist_list.append(gen_dist_from_adjusted_dataset(detecter_net, encoder, adjust_method, features, device))

# plot density curve
# plot_density(dist_list, 'dist_density')

# find best thresholds
calculate_thresholds(gen_dists_and_labels(same_person_dist, different_person_dist))
