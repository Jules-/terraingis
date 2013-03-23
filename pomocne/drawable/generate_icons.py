#! /usr/bin/env python3

from glob import glob
from subprocess import call

TERRAINGIS_DIR = "../../src/TerrainGIS/"

SIZES = {"buttons": {TERRAINGIS_DIR+"res/drawable-ldpi/{}.png": "36",
            TERRAINGIS_DIR+"res/drawable-mdpi/{}.png": "48",
            TERRAINGIS_DIR+"res/drawable-hdpi/{}.png": "72",
            TERRAINGIS_DIR+"res/drawable-xhdpi/{}.png": "96"},
         "icons": {TERRAINGIS_DIR+"res/drawable-ldpi/{}.png": "18",
            TERRAINGIS_DIR+"res/drawable-mdpi/{}.png": "24",
            TERRAINGIS_DIR+"res/drawable-hdpi/{}.png": "36",
            TERRAINGIS_DIR+"res/drawable-xhdpi/{}.png": "48"},
         "logo": {TERRAINGIS_DIR+"res/drawable-ldpi/{}.png": "36",
            TERRAINGIS_DIR+"res/drawable-mdpi/{}.png": "48",
            TERRAINGIS_DIR+"res/drawable-hdpi/{}.png": "72",
            TERRAINGIS_DIR+"res/drawable-xhdpi/{}.png": "96",
            TERRAINGIS_DIR+"{}-web.png": "512"}}

if __name__ == '__main__':
    for dir in SIZES:
        files = glob("{}/*.svg".format(dir))
    
        for file in files:
            out_name = file[len(dir)+1:-4]
            for out_dir in SIZES[dir]:
                args = ("inkscape",
                        "-e", out_dir.format(out_name),
                        "--export-area-page",
                        "-w", SIZES[dir][out_dir],
                        "-h", SIZES[dir][out_dir],
                        file)
                call(args)
