from __future__ import print_function

import argparse
import cv2
import imutils
import numpy as np
import os
from imutils import paths
from imutils.object_detection import non_max_suppression


def shiftRbyn(arr, n=0):
    return arr[n:len(arr):] + arr[0:n:]

# construct the argument parse and parse the arguments
ap = argparse.ArgumentParser()
#ap.add_argument("-i", "--images", required=True, help="path to images directory")
ap.add_argument("-i", "--inimagesinv", required=True, help="path to inverted input sequence")
ap.add_argument("-o", "--outimagesinv", required=True, help="path to inverterd output sequence")
ap.add_argument("-f", "--blurringfactor", default=6, help="scale the blurring kernel depending on the height of a given ROI, default = h/6")
args = vars(ap.parse_args())

# assemnble inverse_dir_path
#inverse_dir_path = args["images"] + "_inverse/"
inverse_dir_path = args["inimagesinv"]

# assemble out_dir_path
#out_dir_path = args["images"] + "_out_inverse/"
out_dir_path = args["outimagesinv"]

# assemnble and inverse_result_dir_path
#inverse_result_dir_path = args["images"] + "_inverse_result/"
inverse_result_dir_path = args["outimagesinv"] + "_result"
os.mkdir(inverse_result_dir_path)

# initialize the HOG descriptor/person detector
hog = cv2.HOGDescriptor()
hog.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())

# 1: paint rectangles around ROIs, 0: don't
paint_rects = 0

# scaling factor for Gaussian blurring kernel relative to the height of a ROI
sf = int(args["blurringfactor"])

k = 1
sws = 3
list_of_picks = np.empty((sws, 0)).tolist()

# loop over the image paths
for imagePath in paths.list_images(inverse_dir_path):
    # load the image and resize it to (1) reduce detection time
    # and (2) improve detection accuracy
    image = cv2.imread(imagePath)
    image = imutils.resize(image, height=min(600, image.shape[0]))
    orig = image.copy()

    # assemble path of the output image in out_inverse_dir
    #print(imagePath)
    out_image_path = imagePath.replace("inverse", "out_inverse")
    #print(out_image_path)

    # load the corresponding output image
    out_image = cv2.imread(out_image_path)
    #cv2.imshow("Output of forward processing", out_image)

    # detect people in the image
    (rects, weights) = hog.detectMultiScale(image, winStride=(4, 4),
                                            padding=(8, 8), scale=1.05)

    # draw the original bounding boxes
    #for (x, y, w, h) in rects:
    #   cv2.rectangle(orig, (x, y), (x + w, y + h), (0, 0, 255), 2)

    # apply non-maxima suppression to the bounding boxes using a
    # fairly large overlap threshold to try to maintain overlapping
    # boxes that are still people
    rects = np.array([[x, y, x + w, y + h] for (x, y, w, h) in rects])
    pick = non_max_suppression(rects, probs=None, overlapThresh=0.65)
    shiftRbyn(list_of_picks, 1)
    list_of_picks.insert(0, pick)
    if k > sws:
        list_of_picks.pop()

    # draw the final bounding boxes
    for picklist in list_of_picks:
        for (xA, yA, xB, yB) in picklist:
            if paint_rects == 1:
                cv2.rectangle(image, (xA, yA), (xB, yB), (0, 255, 0), 2)
            roi = out_image[yA:yB, xA:xB]
            # apply a gaussian blur on this new rectangle image
            h2 = yB - yA
            if int(h2 / sf) % 2:  # odd
                roi = cv2.GaussianBlur(roi, (int(h2 / sf), int(h2 / sf)), 0)
            else:  # even
                roi = cv2.GaussianBlur(roi, (int(h2 / sf) + 1, int(h2 / sf) + 1), 0)
            out_image[yA:yA + roi.shape[0], xA:xA + roi.shape[1]] = roi


    # show the output images
    #cv2.imshow("Before NMS", orig)
    #cv2.imshow("Blurred output", out_image)
    #cv2.waitKey(1)

    #print(inverse_result_dir_path + os.path.basename(imagePath))
    cv2.imwrite(inverse_result_dir_path + "/" + os.path.basename(imagePath), out_image)

    k += 1