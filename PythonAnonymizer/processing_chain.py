import argparse
from os import system

ap = argparse.ArgumentParser()
ap.add_argument("-i", "--images", required=True, help="path to images directory")
ap.add_argument("-f", "--blurringfactor", default=6, help="scale the blurring kernel depending on the height of a given ROI, default = h/6")
args = vars(ap.parse_args())

system("python exporting_detector.py -i {} -f {}".format(args["images"], args["blurringfactor"]))
system("python order_inverter.py -i {} -o {}".format(args["images"], args["images"] + "_out"))
system("python inverse_detector.py -i {} -o {} -f {}".format(args["images"] + "_inverse", args["images"] + "_out_inverse", args["blurringfactor"]))
system("python backwards_inverter.py -i {}".format(args["images"] + "_out_inverse_result"))
