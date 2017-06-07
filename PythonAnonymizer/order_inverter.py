from __future__ import print_function

import argparse
import glob
import os
import shutil

inverse_dir_postfix = "inverse"
out_inverse_dir_postfix = "inverse"

# construct the argument parse and parse the arguments
# TODO: use root path of image_dir
ap = argparse.ArgumentParser()
#ap.add_argument("-i", "--images", required=True, help="path to images directory")
ap.add_argument("-i", "--inimages", required=True, help="path to images directory")
ap.add_argument("-o", "--outimages", required=True, help="path to output images directory of forward processing")
args = vars(ap.parse_args())

inverse_dir_path = args["inimages"] + "_" + inverse_dir_postfix
os.mkdir(inverse_dir_path)

out_inverse_dir_path = args["outimages"] + "_" + out_inverse_dir_postfix
os.mkdir(out_inverse_dir_path)

# get the number of images in the input folder
counter = 0
#listing = os.listdir(args["images"])
listing = os.listdir(args["inimages"])
for filenames in listing:
    if filenames.endswith('.png'):
        counter += 1

counter2 = counter

# rename the input images with inverted indices
files = glob.iglob(os.path.join(args["inimages"], "*.png"))
for file in files:
    if os.path.isfile(file):
        new_filepath = inverse_dir_path + "/" + str(counter).zfill(4) + ".png"
        #print(new_filepath)
        shutil.copy2(file, new_filepath)
        counter -= 1

# rename the output images with inverted indices
#outfiles = glob.iglob(os.path.join(args["images"] + "_out/", "*.png"))
outfiles = glob.iglob(os.path.join(args["outimages"], "*.png"))
for outfile in outfiles:
    if os.path.isfile(outfile):
        new_out_filepath = out_inverse_dir_path + "/" + str(counter2).zfill(4) + ".png"
        #print(new_out_filepath)
        shutil.copy2(outfile, new_out_filepath)
        counter2 -= 1

