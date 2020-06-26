import numpy as np
from scipy import interpolate
from sklearn.model_selection import KFold
import matplotlib.pyplot as plt
from sklearn.metrics import auc
from tqdm import tqdm
from torchvision import datasets, transforms
import torch
import cv2
import seaborn as sns
import scipy.io as sio
from torch.utils.data import DataLoader
from PIL import Image, ImageEnhance
import pickle
import os
import matplotlib
import scipy.misc
import imageio
from copy import deepcopy
from networks.encoder_net import *
from networks.detecter_net import *
import sys
import warnings
from PIL import Image


# calculate the distance between two feature vectors
def tensor_dist(tensor_a, tensor_b, mode='cosine'):
    if mode == 'cosine':
        # print(np.shape(torch.cosine_similarity(tensor_a, tensor_b, dim=0)))
        return torch.cosine_similarity(tensor_a, tensor_b, dim=0).item()
    elif mode == 'l2':
        return (tensor_a - tensor_b).norm().item()
    else:
        print('undefined mode. should be l2 or cosine')


# used for dataset iteration
def collate_fn(x):
    return x[0]


# shift img by a small perturbation
def shift_img(image, x, y):
    M = np.float32([[1, 0, x], [0, 1, y]])
    shifted = cv2.warpAffine(image, M, (np.shape(image)[0], np.shape(image)[1]))
    return shifted


# find the optimal thresholds
def calculate_thresholds(labels, distances):
    threshold_list = [1, 1.1, 1.2, 1.3]
    for threshold in threshold_list:
        print()
        true_positive_num = 0
        true_negative_num = 0
        false_positive_num = 0
        false_negative_num = 0
        for i in range(len(distances)):
            # true positive
            if labels[i] == 1 and distances[i] <= threshold:
                true_positive_num += 1

            # true negative
            elif labels[i] == 1 and distances[i] > threshold:
                true_negative_num += 1

            # false positive
            elif labels[i] == 0 and distances[i] <= threshold:
                false_positive_num += 1

            # false negative
            elif labels[i] == 0 and distances[i] > threshold:
                false_negative_num += 1

        total_true_num = (true_positive_num + true_negative_num)
        total_false_num = (false_positive_num + false_negative_num)

        true_positive_rate = true_positive_num / total_true_num
        true_negative_rate = true_negative_num / total_true_num
        false_positive_rate = false_positive_num / total_false_num
        false_negative_rate = false_negative_num / total_false_num
        print('threshold '+ str(threshold)+'. '+'true_positive_rate: ' + str(true_positive_rate))
        print('threshold '+ str(threshold)+'. '+'true_negative_rate: ' + str(true_negative_rate))
        print('threshold '+ str(threshold)+'. '+'false_positive_rate: ' + str(false_positive_rate))
        print('threshold '+ str(threshold)+'. '+'false_negative_rate: ' + str(false_negative_rate))


# save mat
def save_mat_dict(mat_dict):
    for name, mat in mat_dict.items():
        sio.savemat('results/'+name+'_dist.mat', {name+'_dist': mat})


# currently not in use
def gen_dists_and_labels(same_person_dist, different_person_dist):
    distances = []
    labels = []
    distances.append(same_person_dist)
    distances.append(different_person_dist)
    labels.append(np.ones(np.shape(same_person_dist)))
    labels.append(np.zeros(np.shape(different_person_dist)))
    labels = np.array([sublabel for label in labels for sublabel in label])
    distances = np.array([subdist for dist in distances for subdist in dist])
    sio.savemat('results/' + 'dists.mat', {'dists': distances})
    sio.savemat('results/' + 'labels.mat', {'labels': labels})
    return labels, distances


# compare feature vectors for batches of image
def get_dist(features_1, features_2, dist_mode='cosine', name='', restore=False):
    if restore:
        return sio.loadmat('results/{}_dist.mat'.format(name), squeeze_me=True)['dist']
    dist = []
    for i in tqdm(range(0, len(features_1) - 1), desc='Calculating distances of adjusted face ' + name):
        dist.append(tensor_dist(features_1[i], features_2[i], mode=dist_mode))
    sio.savemat('results/' + name + '_dist.mat', {'dist': dist})
    return dist


# compare feature vectors for same person and different people
def get_dist_same_different(features, names, dist_mode='cosine', restore=False):
    if restore:
        same_person_dist = sio.loadmat('results/' + 'same_person' + '_dist.mat', squeeze_me=True)['dist']
        different_person_dist = sio.loadmat('results/' + 'different_person' + '_dist.mat', squeeze_me=True)['dist']
        return same_person_dist, different_person_dist

    same_person_dist = []
    different_person_dist = []
    for i in tqdm(range(0, len(features) - 1), desc='Calculating distances of same/different person'):
        for j in range(i + 1, len(features)):
            dist = tensor_dist(features[i], features[j], mode=dist_mode)
            if names[i] == names[j]:
                same_person_dist.append(dist)
            else:
                different_person_dist.append(dist)
    print('same pair num: ' + str(np.shape(same_person_dist)) + '.  different pair ' + str(
        np.shape(different_person_dist)))
    sio.savemat('results/' + 'same_person' + '_dist.mat', {'dist': same_person_dist})
    sio.savemat('results/' + 'different_person' + '_dist.mat', {'dist': different_person_dist})

    return same_person_dist, different_person_dist


# given an pil image, extract its feature vector
def get_features_from_pil(detecter, encoder, device, input_root='inputs/a.jpg'):

    img = Image.open(input_root)
    aligned = detecter(img, return_prob=False)
    aligned = torch.stack([aligned]).to(device)
    features = encoder(aligned).detach().cpu()

    return torch.squeeze(features)


# align image
def preprocess_test_imgs(detecter_net, mode='unadjusted', dataset_path='../datasets/lfw',
                         save_root='../datasets/lfw_cropped_list_new/', adjust_std=0.1, restore=False):

    if restore:
        with open(save_root + 'aligned_' + mode +'.pickle', 'rb') as fp:
            return pickle.load(fp)

    dataset = datasets.ImageFolder(dataset_path)
    dataset.idx_to_class = {i:c for c, i in dataset.class_to_idx.items()}
    workers = 0 if os.name == 'nt' else 4
    loader = DataLoader(dataset, collate_fn=collate_fn, batch_size=1, num_workers=workers)

    aligned = []
    for x, y in tqdm(loader, desc='Detecting faces'):

        if mode == 'unadjusted':
            aligned.append(detecter_net(x, return_prob=False))

        elif mode == 'names':
            aligned.append(dataset.idx_to_class[y])

        elif mode == 'brightness':
            aligned.append(detecter_net(ImageEnhance.Brightness(x).enhance(np.random.normal(1, adjust_std)), return_prob=False))

        elif mode == 'balance':
            aligned.append(detecter_net(ImageEnhance.Color(x).enhance(np.random.normal(1, adjust_std)), return_prob=False))

        elif mode == 'contrast':
            aligned.append(detecter_net(ImageEnhance.Contrast(x).enhance(np.random.normal(1, adjust_std)), return_prob=False))

        elif mode == 'downsampled':
            aligned.append(detecter_net(cv2.resize(
                cv2.resize(np.asarray(x), (int(np.shape(x)[0] / 2), int(np.shape(x)[0] / 2)), interpolation=cv2.INTER_NEAREST),
                (int(np.shape(x)[0]), int(np.shape(x)[0])), interpolation=cv2.INTER_NEAREST), return_prob=False))
        elif mode == 'shifted':
            aligned.append(detecter_net(shift_img(np.asarray(x), 25, 25), return_prob=False))
        else:
            print('Undefined mode! Should be one of unadjusted, names brightness, balance, '
                  'contrast, downsampled, or shifted.')
            exit()
    with open(save_root + 'aligned_' + mode + '.pickle', 'wb') as fp:
        pickle.dump(aligned, fp)

    return aligned


# format conversion
def tensor_to_PIL(tensor):
    loader = transforms.Compose([transforms.ToTensor()])
    unloader = transforms.ToPILImage()
    image = tensor.cpu().clone()
    image = image.squeeze(0)
    image = unloader(image)
    return image


# format conversion
def PIL_to_tensor(image, device):
    loader = transforms.Compose([transforms.ToTensor()])
    unloader = transforms.ToPILImage()
    image = loader(image).unsqueeze(0)
    return image.to(device, torch.float)


# display image
def imshow(tensor, title=None):
    loader = transforms.Compose([transforms.ToTensor()])
    unloader = transforms.ToPILImage()
    image = tensor.cpu().clone()  # we clone the tensor to not do changes on it
    image = image.squeeze(0)  # remove the fake batch dimension
    image = unloader(image)
    plt.imshow(image)
    if title is not None:
        plt.title(title)
    plt.pause(0.001)  # pause a bit so that plots are updated


# format conversion
def tensor2numpy(tensor):
    return np.squeeze(np.asarray(tensor.detach().cpu())).transpose(1, 2, 0)


# generate plot for prediction
def plot_images(X, y, yp, M, N):
    f,ax = plt.subplots(M, N, sharex=True, sharey=True, figsize=(N, M*1.3))
    for i in range(M):
        for j in range(N):
            ax[i][j].imshow(1-X[i*N+j][0].cpu().numpy(), cmap="gray")
            title = ax[i][j].set_title("Pred: {}".format(yp[i*N+j].max(dim=0)[1]))
            plt.setp(title, color=('g' if yp[i*N+j].max(dim=0)[1] == y[i*N+j] else 'r'))
            ax[i][j].set_axis_off()
    plt.tight_layout()


# extract feature vectors
def get_features(aligned, encoder_net, device, batch_size=200, tqdm_name='', save_root='../datasets/lfw_cropped_list_new/', restore=False):
    if restore:
        with open(save_root + 'features_' + tqdm_name + '.pickle', 'rb') as fp:
            return pickle.load(fp)

    num_batch = len(aligned) // batch_size
    features = torch.zeros(0, 512)
    for i in tqdm(range(num_batch), desc='Calculating features ' + tqdm_name):
        features = torch.cat((features, encoder_net(torch.stack(aligned)[i * batch_size:(i + 1) * batch_size, ...].to(device)).detach().cpu()), dim=0)

    with open(save_root + 'features_' + tqdm_name+ '.pickle', 'wb') as fp:
        pickle.dump(features, fp)
    return features


# initialize nets, must be run in the very begining
def initialize_nets():
    if not sys.warnoptions:
        warnings.simplefilter("ignore")
    device = torch.device('cuda:7' if torch.cuda.is_available() else 'cpu')
    print('Running on device: {}'.format(device))

    detecter = detecter_net(image_size=160, margin=0, min_face_size=20, thresholds=[0.6, 0.7, 0.7],
                            factor=0.709, post_process=True, device=device)

    encoder = encoder_net(pretrained='vggface2').eval().to(device)

    return detecter, encoder, device

