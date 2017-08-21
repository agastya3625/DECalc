#library required for running DESeq2
library(BiocGenerics)
# sink messages to stdout
sink(stdout(), type = "message")
#command line array passed in by a Bash file.
#args has the following format:
#<data file name> <metadata file name> <comparisons to run> <folder to put DESeq2 data into> <levels of Type>

args <-  commandArgs(trailingOnly = TRUE)
data_file <- args[1]
metadata_file <- args[2]
data <- tail(unlist(strsplit(metadata_file, split = "_", fixed = TRUE)), n=1)
comps <- args[3]
outfile <- args[4]
data_file_name <- sub(".csv", "", data)
conts = strsplit(args[5], ",", fixed = TRUE)
levsTreatment = unlist(conts)
levs = strsplit(args[6], ",", fixed = TRUE)
levsType = unlist(levs)


message("=========================================================")

####### Make Data Frames #######
Count_Data <- read.csv(data_file)
counts <- data.frame(Count_Data, row.names = 1)
Meta_Data_all <- read.csv(metadata_file)
sampleNames <- data.frame(Meta_Data_all, row.names = 1)

#checks to see if the metadata file row names are the same as the data file column names
if (all(rownames(sampleNames) == colnames(counts)) == FALSE) {
  message("ERROR: data matrix columns do not match metadata rows.")
  stop()
}

####### Run analysis #######
dds <-
  DESeq2::DESeqDataSetFromMatrix(
    countData = counts,
    colData = sampleNames,
    design = ~ Treatment + Type
  )
#making of the design formula
dds$Type <- factor(dds$Type, levels = levsType)
dds$Treatment <-
  factor(dds$Treatment, levels = levsTreatment)
dds$group <- factor(paste0(dds$Treatment, dds$Type))
design(dds) <- ~ group
#runs normalization
ddsRES <- DESeq2::DESeq(dds)

####### Extract Data #######

#converts the comparisons passed in into an array
d = unlist(strsplit(comps, "-", fixed = TRUE))
DESeq2::resultsNames(ddsRES)

#loops through the array of comparisons and performs each one
for (i in d) {
  tryCatch({
    #first variable to compare
    cont = unlist(strsplit(i, "vs"))[1]
    #second comparison variable
    vars = unlist(strsplit(i, "vs"))[2]
    #creates data file name
    file_name <- paste(data_file_name, "_", i, sep = "")
    sigOut <- paste(file_name, "_SIG.csv", sep = "")
    resOut <- paste(file_name, "_RES.csv", sep = "")
    resFile <- paste(outfile, "/", resOut, sep = "")
    sigFile <- paste(outfile, "/", sigOut, sep = "")
    #gets results
    results_final <-
      DESeq2::results(ddsRES, contrast = c("group", cont, vars))
    write.csv(as.data.frame(results_final), file = resFile)
    # Get Significant Genes
    res21IF_21UF_Sig1 <- subset(results_final, log2FoldChange > 1)
    res21IF_21UF_Sig2 <- subset(results_final, log2FoldChange < -1)
    res21IF_21UF_Sig <- rbind(res21IF_21UF_Sig1, res21IF_21UF_Sig2)
    write.csv(as.data.frame(res21IF_21UF_Sig), file = sigFile)
    #resets output file names
    sigOut <- ""
    resOut <- ""
    resFile <- ""
    sigFile <- ""
    message(paste("Comparison ", i, ": passed", sep = ""))
    }, warning = function(war) {
    message(paste("Comparison ", i, ": failed", sep = ""))
    message(war)
    }, error = function(err) {
    message(paste("Comparison ", i, ": failed", sep = ""))
    message(err)
    })
}
