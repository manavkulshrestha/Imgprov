from eval import *

# initialize nets, must be run in the very begining
detecter_net, encoder_net, device = initialize_nets()

# extract feature vectors of two input images
feature_vector_a = get_features_from_pil(detecter_net, encoder_net, device, input_root='inputs/a.jpg')
feature_vector_b = get_features_from_pil(detecter_net, encoder_net, device, input_root='inputs/b.jpg')

# print the similarity between the two images by their feature vectors
dist = tensor_dist(feature_vector_a, feature_vector_b)
print(dist)
