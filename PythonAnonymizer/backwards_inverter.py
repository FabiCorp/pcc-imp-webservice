from __future__ import print_function

import argparse
import glob
import os
import shutil

inverse_dir_name = "inverse"
out_inverse_dir_name = "out_inverse"

# construct the argument parse and parse the arguments
# TODO: use root path of image_dir
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--images", required=True, help="path to images directory")
args = vars(ap.parse_args())

input_dir_path = args["images"]

result_dir_path = args["images"] + "_inverted"
os.mkdir(result_dir_path)

# get the number of images in the input folder
counter = 0
listing = os.listdir(args["images"])
for filenames in listing:
    if filenames.endswith('.png'):
        counter += 1

# rename the input images with inverted indices
files = glob.iglob(os.path.join(args["images"], "*.png"))
for file in files:
    if os.path.isfile(file):
        new_filepath = result_dir_path + "/" + str(counter).zfill(4) + ".png"
        #print(new_filepath)
        shutil.copy2(file, new_filepath)
        counter -= 1

