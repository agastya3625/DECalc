#library required for running DESeq2
startTime <- Sys.time()
library(BiocGenerics)
sendtoTwoPlaces <- function(logfilename, message){
  message(message)
  cat(message, file = logfilename, sep = "\n", append = TRUE)
}
# sink messages to stdout
sink(stdout(), type = "message")
#command line array passed in by a Bash file.
#args has the following format:
#<data file name> <metadata file name> <comparisons to run> <folder to put DESeq2 data into> <levels of Type>
args <- commandArgs(trailingOnly = TRUE) #
#args <- array(data = c("/Users/agastya/Desktop/Data/Ctrl exp cts.csv","/Users/agastya/Desktop/metadata/metadata_Ctrl.csv", "AvsB-AvsC-AvsD-AvsE-BvsC-BvsD-BvsE-CvsD-CvsE-DvsE", "/Users/agastya/Desktop/TestFileCat", "A,B,C,D,E")) #
data_file <- args[1]
metadata_file <- args[2]
data <- tail(unlist(strsplit(metadata_file, split = "_", fixed = TRUE)), n=1)
comps <- args[3]
outfile <- args[4]
data_file_name <- sub(".csv", "", data)
# conts = strsplit(args[5], ",", fixed = TRUE)
# levsTreatment = unlist(conts)
levs = strsplit(args[5], ",", fixed = TRUE)
levsType = unlist(levs)
# # third = strsplit(args[7], ",", fixed = TRUE)
# # levsTime = unlist(third)


######### MAKE LOG FILE #########
logfilename <- paste(outfile,"/DESeq Run Log.txt", sep = "")
#logfilename <- "/Users/agastya/Desktop/DESeq Run Log.txt"
if(!file.exists(logfilename)){
  file.create(logfilename)
}
cat(paste("Date and time of run start: ",Sys.time(), "\n", "\n######## Run Information #########\n", "Data File: ", data_file, "\nmetadata file: ",metadata_file,"\nResults placed in: ",outfile,sep = ""), file= logfilename, sep = "", append = TRUE)
cat(paste("\n\n\n######## DESeq2 Script Information #########\n", "Command Line for metadata file: ", metadata_file, "\n", paste(args, collapse = " "),sep = ""), file = logfilename, sep = "", append = T)
sendtoTwoPlaces(logfilename, "\n\n=========================================================")


####### Make Data Frames #######
counts <- data.frame(read.csv(data_file), row.names = 1)
sampleNames <- data.frame(read.csv(metadata_file), row.names = 1)

#checks to see if the metadata file row names are the same as the data file column names
if (all(rownames(sampleNames) == colnames(counts)) == FALSE) {
  message("data matrix columns do not match metadata rows, reordering")
  counts <- counts[,match(rownames(sampleNames), colnames(counts))]
}

####### Run analysis #######
dds <-
  DESeq2::DESeqDataSetFromMatrix(
    countData = counts,
    colData = sampleNames,
    design = ~ Type
  )
#making of the design formula
dds$Type <- factor(dds$Type, levels = levsType)
# dds$Treatment <-
#   factor(dds$Treatment, levels = levsTreatment)
#dds$Time <- factor(dds$Time, levels = levsTime)
#dds$group <- factor(paste0(dds$Treatment, dds$Type))
#design(dds) <- ~ group
#runs normalization
ddsRES <- DESeq2::DESeq(dds)
####### Extract Data #######

#converts the comparisons passed in into an array
comps <- gsub("'", "", comps)
d = unlist(strsplit(comps, "-", fixed = TRUE))
DESeq2::resultsNames(ddsRES)

outname <- paste(outfile, "/DESeq Results", sep = "")
if(!dir.exists(outname)){
  dir.create(outname)
}
#loops through the array of comparisons and performs each one
for (i in d) {
  tryCatch({
    j <- match(i, d)
    #first variable to compare
    cont = unlist(strsplit(i, "vs"))[1]
    #second comparison variable
    vars = unlist(strsplit(i, "vs"))[2]
    check <- paste("Type_",cont,"_vs_",vars, sep = "")
    if(check %in% DESeq2::resultsNames(ddsRES)){
      results_final <-
        DESeq2::results(ddsRES, contrast = c("Type", cont, vars))
      files <- i
    } else{
      results_final <-
        DESeq2::results(ddsRES, contrast = c("Type", vars, cont))
      files <- paste(vars, cont, sep = "vs")
    }
    #creates data file name
    file_name <- paste(data_file_name, "_", files, sep = "")
    resFile <- paste(outname, "/", file_name, "_RES.csv", sep = "")
    sigFile <- paste(outname, "/", file_name, "_SIG.csv",sep = "")
    #gets results
    write.csv(as.data.frame(results_final), file = resFile)
    #Get Significant Genes
    res21IF_21UF_Sig <- subset(results_final, padj <= 0.1)
    res21IF_21UF_Sig <- subset(res21IF_21UF_Sig, abs(log2FoldChange) >= 1)
    write.csv(as.data.frame(res21IF_21UF_Sig), file = sigFile)
    #resets output file names
    resFile <- ""
    sigFile <- ""
    sendtoTwoPlaces(logfilename, paste("Comparison ", files, ": passed (Comparison ",j," of ",length(d),")", sep = ""))
  }, warning = function(war) {
    sendtoTwoPlaces(logfilename, paste("Comparison ", files, ": failed (Comparison ",j," of ",length(d),")", sep = ""))
    sendtoTwoPlaces(logfilename, war)
  }, error = function(err) {
    sendtoTwoPlaces(logfilename, paste("Comparison ", files, ": failed (Comparison ",j," of ",length(d),")", sep = ""))
    sendtoTwoPlaces(logfilename, err)
  })
}
time <- Sys.time() - startTime
cat(paste("\n######## Script Finished #########\nDate and Time of Script End: ", Sys.time(),sep = ""), file = logfilename, append = T)