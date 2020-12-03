all: timezonedb.idx

download:
	-@rm -f tzdata20[12]*tar.gz
	-@rm -f tzcode20[12]*tar.gz
	@echo "Downloading latest Olson TZDB release..."
	$(eval VERSION := $(shell curl -s -o - https://www.iana.org/time-zones | grep \"version\" | sed 's/.*version">//' | sed 's/<\/span.*//'))
	curl -s -o tzdata$(VERSION).tar.gz https://data.iana.org/time-zones/releases/tzdata$(VERSION).tar.gz
	curl -s -o tzcode$(VERSION).tar.gz https://data.iana.org/time-zones/releases/tzcode$(VERSION).tar.gz
	@echo " done"

version-info.txt: download
	find . -name tzdata*.tar.gz | sed 's/.*202/202/' | sed 's/\.tar.*//' > version-info.txt
	find . -name tzdata*.tar.gz | sed -E "s/^.*(20[0-9]{2}).*$$/\1/" | tr -d '\n' >> version-info.txt
	echo -n "." >> version-info.txt
	find . -name tzdata*.tar.gz | sed -E "s/^.*20[0-9]{2}(\w).*$$/\1/" | sed "s/\\n//" | od -A n -t d1 -N 1 | awk '{printf "%s", $$1 - 96}' >> version-info.txt

tzdb: download version-info.txt
	-@rm -rf code
	@mkdir code
	@echo "Unpacking Olson TZDB release..."
	tar -C code -xzf tzdata*tar.gz
	tar -C code -xzf tzcode*tar.gz

code/zone.tab: tzdb
	@make -C code
	@echo "Compiling tzdata files..."
	@for i in africa antarctica australasia asia etcetera europe factory northamerica southamerica backward; do \
		cat code/$$i | ./code/zic -b fat -d code/data -; \
		echo "- $$i"; \
	done;

timezonedb.dta: code/zone.tab
	php gentzinfo.php

gentzinfo:
	php gentzinfo.php

gentzmapping:
	php gentzmapping.php

timezonedb.idx: timezonedb.dta gentzmapping

timezonedb.zip: code/zone.tab version-info.txt
	@echo -n "Making archive..."
	-@mkdir code/timezonedb-$(VERSION); cp -r code/data/. code/timezonedb-$(VERSION)/; cp version-info.txt code/timezonedb-$(VERSION)/
	-@cd code; zip --quiet -r ../timezonedb.zip timezonedb-$(VERSION)
	@cp timezonedb.zip timezonedb-$(VERSION).zip
	@echo " done"

clean:
	-rm -rf code
	-rm -f timezonedb.dta version-info.txt timezonedb-20[12]*.tgz