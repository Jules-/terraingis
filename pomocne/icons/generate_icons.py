#! /usr/bin/env python3

from glob import glob
from subprocess import call

SIZES = {"ldpi": "18", "mdpi": "24", "hdpi": "36", "xhdpi": "48"}

if __name__ == '__main__':
    files = glob("*.svg")
    
    for file in files:
        png_file = file.replace("svg", "png")
        for dpi in SIZES:
            args = ("inkscape",
                    "-e", "../../src/TerrainGIS/res/drawable-{}/{}".format(dpi, png_file),
                    "--export-area-page",
                    "-w", SIZES[dpi],
                    "-h", SIZES[dpi],
                    file)
            call(args)
