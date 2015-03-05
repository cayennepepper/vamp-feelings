from xml.dom import minidom
import sys


file_name = sys.argv[1]
xmldoc = minidom.parse(file_name)
itemList = xmldoc.getElementsByTagName('token')

#This is the dictionary of indices we are returning (name -> location in text)
nameIndicesDict = {}

#This grabs all tokens which have an <NER> Tag containing 'PERSON' value.
#It sticks it into the nameIndicesDict dictionary - key name, list[value pair of beginChar, endChar]
for token in itemList:
	nerNodes = token.getElementsByTagName('NER')
	for nerNode in nerNodes:
		if nerNode.firstChild.nodeValue == "PERSON": #Get name, start char, end char
			personName = token.getElementsByTagName('word')[0].firstChild.nodeValue
			startChar = token.getElementsByTagName('CharacterOffsetBegin')[0].firstChild.nodeValue
			endChar = token.getElementsByTagName('CharacterOffsetEnd')[0].firstChild.nodeValue
			if nameIndicesDict.get(personName) is None:
				nameIndicesDict[personName] = [(int(startChar), int(endChar))]
			else:
				nameIndicesDict[personName].append((int(startChar), int(endChar)))

for k,v in nameIndicesDict.items():
	print k
	print v




# print(itemlist[0].attributes['id'].value)
# print "blah"
# for s in itemlist:
# 	print(s.attributes['id'].value)



