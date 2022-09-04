SOURCES= \
	JFontChooserMessages_ja_JP.utf8 \

RESOURCES= $(SOURCES:.utf8=.properties)

CONVERTER = native2ascii
SOURCE_ENCODING = UTF8
FLAG = -encoding $(SOURCE_ENCODING)

COPY = cp

all : $(RESOURCES)

.SUFFIXES: .properties .utf8

.utf8.properties : 
	$(CONVERTER) $(FLAG) $<  $@

clean :
	rm $(RESOURCES)

