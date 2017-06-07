from __future__ import print_function
from imutils import paths
import argparse
import cv2

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
ap.add_argument("-i", "--images", required=True, help="path to images directory")
ap.add_argument("-w", "--waitkey", default=100, help="time in ms for showing each frame, default=100")
args = vars(ap.parse_args())

for imagePath in paths.list_images(args["images"]):
    image = cv2.imread(imagePath)
    cv2.imshow("Output", image)
    cv2.waitKey(int(args["waitkey"]))