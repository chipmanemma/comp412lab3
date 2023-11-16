# Compiler flags
JFLAGS = -g
JC = javac
.SUFFIXES: .java .class

# Target entry for creating .class files from .java files
build: Main2.java
	$(JC) $(JFLAGS) Main2.java CatLex.java ILOCParser.java ILOCScanner.java ILOCRenamer.java ILOCScheduler.java IRList.java IRNode.java OpCategory.java OpCode.java Pair.java DepGraphNode.java OpInfoEnum.java

# RM is a predefined macro in make (RM = rm -f)
clean:
	$(RM) *.class