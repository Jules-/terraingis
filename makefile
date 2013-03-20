PROGRAM_NAME = TerrainGIS

.PHONY: pack clean

pack:
	git ls-files | xargs zip -r $(PROGRAM_NAME)_$$(date "+%F").zip .git/

clean:
	$(RM) *.zip