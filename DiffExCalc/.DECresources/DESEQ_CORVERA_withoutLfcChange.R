#library required for running DESeq2
library(BiocGenerics)
# sink messages to stdout
sink(stdout(), type = "message")
#command line array passed in by a Bash file.
#args has the following format:
#<data file name> <metadata file name> <comparisons to run> <folder to put DESeq2 data into> <levels of Type>

args <- commandArgs(trailingOnly = TRUE) #array(data = c("/Users/agastya/Desktop/Controls_expected_matrix.csv","/Users/agastya/Desktop/metadata_controls_fsk.csv",'ControlAvsControlB',"/Users/agastya/Desktop/test","'Control','Forskolin'","'A','B','C','D','E','F'")) #
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
# # third = strsplit(args[7], ",", fixed = TRUE)
# # levsTime = unlist(third)

#'ControlAvsControlB'-'ControlAvsControlC'-'ControlAvsControlD'-'ControlAvsControlE'-'ControlAvsControlF'-'ControlAvsForskolinA'-'ControlAvsForskolinB'-'ControlAvsForskolinC'-'ControlAvsForskolinD'-'ControlAvsForskolinE'-'ControlAvsForskolinF'-'ControlBvsControlC'-'ControlBvsControlD'-'ControlBvsControlE'-'ControlBvsControlF'-'ControlBvsForskolinA'-'ControlBvsForskolinB'-'ControlBvsForskolinC'-'ControlBvsForskolinD'-'ControlBvsForskolinE'-'ControlBvsForskolinF'-'ControlCvsControlD'-'ControlCvsControlE'-'ControlCvsControlF'-'ControlCvsForskolinA'-'ControlCvsForskolinB'-'ControlCvsForskolinC'-'ControlCvsForskolinD'-'ControlCvsForskolinE'-'ControlCvsForskolinF'-'ControlDvsControlE'-'ControlDvsControlF'-'ControlDvsForskolinA'-'ControlDvsForskolinB'-'ControlDvsForskolinC'-'ControlDvsForskolinD'-'ControlDvsForskolinE'-'ControlDvsForskolinF'-'ControlEvsControlF'-'ControlEvsForskolinA'-'ControlEvsForskolinB'-'ControlEvsForskolinC'-'ControlEvsForskolinD'-'ControlEvsForskolinE'-'ControlEvsForskolinF'-'ControlFvsForskolinA'-'ControlFvsForskolinB'-'ControlFvsForskolinC'-'ControlFvsForskolinD'-'ControlFvsForskolinE'-'ControlFvsForskolinF'-'ForskolinAvsForskolinB'-'ForskolinAvsForskolinC'-'ForskolinAvsForskolinD'-'ForskolinAvsForskolinE'-'ForskolinAvsForskolinF'-'ForskolinBvsForskolinC'-'ForskolinBvsForskolinD'-'ForskolinBvsForskolinE'-'ForskolinBvsForskolinF'-'ForskolinCvsForskolinD'-'ForskolinCvsForskolinE'-'ForskolinCvsForskolinF'-'ForskolinDvsForskolinE'-'ForskolinDvsForskolinF'-'ForskolinEvsForskolinF' "/Users/agastya/Desktop/test" 'Control','Forskolin' 

# data_file <- "/Users/agastya/Desktop/Controls_expected_matrix.csv"
# metadata_file <- "/Users/agastya/Desktop/metadata_controls_fsk.csv"
# data <- tail(unlist(strsplit(metadata_file, split = "_", fixed = TRUE)), n=1)
# comps <- "'ControlAvsControlB'-'ControlAvsControlC'-'ControlAvsControlD'-'ControlAvsControlE'-'ControlAvsControlF'-'ControlAvsForskolinA'-'ControlAvsForskolinB'-'ControlAvsForskolinC'-'ControlAvsForskolinD'-'ControlAvsForskolinE'-'ControlAvsForskolinF'-'ControlBvsControlC'-'ControlBvsControlD'-'ControlBvsControlE'-'ControlBvsControlF'-'ControlBvsForskolinA'-'ControlBvsForskolinB'-'ControlBvsForskolinC'-'ControlBvsForskolinD'-'ControlBvsForskolinE'-'ControlBvsForskolinF'-'ControlCvsControlD'-'ControlCvsControlE'-'ControlCvsControlF'-'ControlCvsForskolinA'-'ControlCvsForskolinB'-'ControlCvsForskolinC'-'ControlCvsForskolinD'-'ControlCvsForskolinE'-'ControlCvsForskolinF'-'ControlDvsControlE'-'ControlDvsControlF'-'ControlDvsForskolinA'-'ControlDvsForskolinB'-'ControlDvsForskolinC'-'ControlDvsForskolinD'-'ControlDvsForskolinE'-'ControlDvsForskolinF'-'ControlEvsControlF'-'ControlEvsForskolinA'-'ControlEvsForskolinB'-'ControlEvsForskolinC'-'ControlEvsForskolinD'-'ControlEvsForskolinE'-'ControlEvsForskolinF'-'ControlFvsForskolinA'-'ControlFvsForskolinB'-'ControlFvsForskolinC'-'ControlFvsForskolinD'-'ControlFvsForskolinE'-'ControlFvsForskolinF'-'ForskolinAvsForskolinB'-'ForskolinAvsForskolinC'-'ForskolinAvsForskolinD'-'ForskolinAvsForskolinE'-'ForskolinAvsForskolinF'-'ForskolinBvsForskolinC'-'ForskolinBvsForskolinD'-'ForskolinBvsForskolinE'-'ForskolinBvsForskolinF'-'ForskolinCvsForskolinD'-'ForskolinCvsForskolinE'-'ForskolinCvsForskolinF'-'ForskolinDvsForskolinE'-'ForskolinDvsForskolinF'-'ForskolinEvsForskolinF'"
# outfile <- "/Users/agastya/Desktop/test/"
# data_file_name <- sub(".csv", "", data)
# conts = strsplit("'A','B','C','D','E','F'", ",", fixed = TRUE)
# levsType = unlist(conts)
# levs = strsplit("'Control','Forskolin'", ",", fixed = TRUE)
# levsTreatment = unlist(levs)

message("=========================================================")

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
    design = ~ Treatment + Type
  )
#making of the design formula
dds$Type <- factor(dds$Type, levels = levsType)
dds$Treatment <-
  factor(dds$Treatment, levels = levsTreatment)
#dds$Time <- factor(dds$Time, levels = levsTime)
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
    #Get Significant Genes
    res21IF_21UF_Sig1 <- subset(results_final, log2FoldChange >= 1)
    res21IF_21UF_Sig2 <- subset(results_final, log2FoldChange <= -1)
    res21IF_21UF_Sig3 <- subset(res21IF_21UF_Sig1, padj <= 0.1)
    res21IF_21UF_Sig4 <- subset(res21IF_21UF_Sig2, padj <= 0.1)
    res21IF_21UF_Sig <- rbind(res21IF_21UF_Sig3, res21IF_21UF_Sig4)
    write.csv(as.data.frame(res21IF_21UF_Sig), file = sigFile)
    #resets output file names
    sigOut <- ""
    resOut <- ""
    resFile <- ""
    sigFile <- ""
    
    ############## TopaSeq ###################
    # airSE <- as(dds, "RangedSummarizedExperiment")
    # dim(airSE)
    # assay(airSE)[1:4,1:4]

    ## ----pdataAirway-----------------------------------------------------------
    # airSE$GROUP <- ifelse(airSE$dex == "trt", 1, 0)
    # table(airSE$GROUP)

    ## ----pdataAirway2----------------------------------------------------------
    # airSE$BLOCK <- airway$cell
    # table(airSE$BLOCK)

    ## ----deAirway--------------------------------------------------------------
    # library(EnrichmentBrowser)
    # #test <- data.matrix(airSE, rownames.force = rownames(AirSE))
    # airSE <- deAna(airSE, de.method="DESeq")
    # rowData(airSE, use.names=TRUE)

    ## ----pwys------------------------------------------------------------------
    # library(graphite)
    # pwys <- pathways(species="hsapiens", database="kegg")
    # pwys
    # 
    # ## ----nodes-----------------------------------------------------------------
    # nodes(pwys[[1]])
    # 
    # ## ----mapIDs----------------------------------------------------------------
    # airSE <- idMap(airSE, org="hsa", from="ALIAS", to="ENTREZID")
    # 
    # ## ----genes-----------------------------------------------------------------
    # all <- names(airSE)
    # de.ind <- rowData(airSE)$padj < 0.1
    # de <- rowData(airSE)$FC[de.ind]
    # names(de) <- all[de.ind]
    # 
    # ## ----nrGenes---------------------------------------------------------------
    # length(all)
    # length(de)
    # 
    # ## ----prs-------------------------------------------------------------------
    # res <- prs(de, all, pwys[1:100], nperm=100)
    # head(res)
    # 
    # ## ----prsWeights------------------------------------------------------------
    # ind <- grep("Ras signaling pathway", names(pwys))
    # weights <- prsWeights(pwys[[ind]], de, all)
    # weights
    # 
    # ## ----maxWeight-------------------------------------------------------------
    # weights[weights == max(weights)]
    # 
    # 
    # 
    # pwys <- pathways(species="hsapiens", database="kegg")
    # pwys
    # 
    # ## ----nodes-----------------------------------------------------------------
    # nodes(pwys[[1]])
    # 
    # ## ----mapIDs----------------------------------------------------------------
    # all <- idMap(res21IF_21UF_Sig, org="hsa", from="ALIAS", to="ENTREZID")

    ## ----genes-----------------------------------------------------------------
    # all <- rownames(res21IF_21UF_Sig)
    # all <- mget(x=rownames(res21IF_21UF_Sig),envir=org.Hs.egALIAS2EG)
    # i = 1
    # gene <- vector("list", length(all))
    # nums <- vector("list", length(all))
    # for(x in all){
    #   if(length(x) >1){
    #     print(x)
    #     print(x[1])
    #     gene[[i]] <- x[1]
    #     nums[[i]] <- i
    #   }
    #   i = i + 1
    # }
    # gene <- gene[!sapply(gene, is.null)]    
    # nums <- nums[!sapply(nums, is.null)]    
    # gene <- unlist(gene)
    # nums <- unlist(nums)
    # all <- replace(all, nums, gene)
    #de <- res21IF_21UF_Sig$log2FoldChange
    #-------------------------------------------
    # length(all)
    # length(de)
    # 
    # ## ----prs-------------------------------------------------------------------
    # res <- prs(de, all, pwys, nperm=100)
    # head(res)
    # 
    # ## ----prsWeights------------------------------------------------------------
    # ind <- grep("Ras signaling pathway", names(pwys))
    # weights <- prsWeights(pwys[[ind]], de, all)
    # weights
    # 
    # ## ----maxWeight-------------------------------------------------------------
    # weights[weights == max(weights)]
    
    
    ########## ENRICHMENTBROWSER ############
    
    
    ## ----style-knitr, eval=TRUE, echo=FALSE, results="asis"--------------------
    #BiocStyle::latex()
    
    ## ----setup, echo=FALSE-----------------------------------------------------
    # suppressMessages(suppressWarnings(suppressPackageStartupMessages({
    #   library(EnrichmentBrowser)
    #   library(ALL)
    #   library(airway)
    #   library(edgeR)
    #   library(limma)
    # })))
    # 
    # ## ----readSE----------------------------------------------------------------
    # library(EnrichmentBrowser)
    # data.dir <- system.file("extdata", package="EnrichmentBrowser")
    # exprs.file <- file.path(data.dir, "exprs.tab")
    # cdat.file <- file.path(data.dir, "colData.tab")
    # rdat.file <- file.path(data.dir, "rowData.tab")
    # se <- readSE(exprs.file, cdat.file, rdat.file)
    # 
    # ## ----sexp2eset-------------------------------------------------------------
    # eset <- as(se, "ExpressionSet")
    # 
    # ## ----eset2sexp-------------------------------------------------------------
    # se <- as(eset, "RangedSummarizedExperiment")
    # 
    # ## ----load-ALL--------------------------------------------------------------
    # library(ALL)
    # data(ALL)
    # 
    # ## ----subset-ALL------------------------------------------------------------
    # ind.bs <- grep("^B", ALL$BT)
    # ind.mut <- which(ALL$mol.biol %in% c("BCR/ABL", "NEG"))
    # sset <- intersect(ind.bs, ind.mut)
    # all.eset <- ALL[, sset]
    # 
    # ## ----show-ALL--------------------------------------------------------------
    # dim(all.eset)
    # exprs(all.eset)[1:4,1:4]
    # 
    # ## ----probe2gene------------------------------------------------------------
    # allSE <- probe2gene(all.eset)
    # head(rownames(allSE))
    # 
    # ----show-probe2gene-------------------------------------------------------
    # rowData(se, use.names=TRUE)
    # 
    # ## ----load-airway-----------------------------------------------------------
    # library(airway)
    # data(airway)
    # 
    # ## ----preproc-airway--------------------------------------------------------
    # airSE <- airway[grep("^ENSG", rownames(airway)),]
    # airSE <- airSE[rowMeans(assay(airSE)) > 10,]
    # dim(airSE)
    # assay(airSE)[1:4,1:4]
    # ## ----sample-groups-ALL-----------------------------------------------------
    # allSE$GROUP <- ifelse(allSE$mol.biol == "BCR/ABL", 1, 0)
    # table(allSE$GROUP)
    # 
    # ## ----sample-groups-airway--------------------------------------------------
    # airSE$GROUP <- ifelse(airway$dex == "trt", 1, 0)
    # table(airSE$GROUP)
    # 
    # ## ----sample-blocks---------------------------------------------------------
    # airSE$BLOCK <- airway$cell
    # table(airSE$BLOCK)
    # 
    # ## ----DE-ana-ALL------------------------------------------------------------
    # allSE <- deAna(allSE)
    # rowData(allSE, use.names=TRUE)
    # 
    # ## ----plot-DE, fig.width=12, fig.height=6-----------------------------------
    # par(mfrow=c(1,2))
    # pdistr(rowData(allSE)$ADJ.PVAL)
    # volcano(rowData(allSE)$FC, rowData(allSE)$ADJ.PVAL)
    # 
    # ## ----DE-exmpl--------------------------------------------------------------
    # ind.min <- which.min( rowData(allSE)$ADJ.PVAL )
    # rowData(allSE, use.names=TRUE)[ ind.min, ]
    # 
    # ## ----DE-ana-airway---------------------------------------------------------
    # airSE <- deAna(airSE, de.method="edgeR")
    # rowData(airSE, use.names=TRUE)
    # 
    # ## ----idmap-idtypes---------------------------------------------------------
    # idTypes("hsa")
    # 
    # ## ----idmap-airway----------------------------------------------------------
    # head(rownames(airSE))
    # airSE <- idMap(airSE, org="hsa", from="ALIAS", to="ENTREZID")
    # head(rownames(airSE))
    # 
    # ## ----parseGMT--------------------------------------------------------------
    # gmt.file <- file.path(data.dir, "hsa_kegg_gs.gmt")
    # hsa.gs <- getGenesets(gmt.file)
    # length(hsa.gs)
    # hsa.gs[1:2]
    # ## ----compile-grn-----------------------------------------------------------
    # hsa.grn <- compileGRN(org="hsa", db="kegg")
    # head(hsa.grn)
    # 
    # ## ----nbeaMethods-----------------------------------------------------------
    # nbeaMethods()
    # 
    # ## ----nbea------------------------------------------------------------------
    # nbea.res <- nbea(method="ggea", se=allSE, gs=hsa.gs, grn=hsa.grn)
    # gsRanking(nbea.res)
    # 
    # ## ----ggea-graph, fig.width=12, fig.height=6--------------------------------
    # par(mfrow=c(1,2))
    # ggeaGraph(
    #   gs=hsa.gs[["hsa05217_Basal_cell_carcinoma"]], 
    #   grn=hsa.grn, se=allSE)
    # ggeaGraphLegend()

    
    
    
    
    
    message(paste("Comparison ", i, ": passed", sep = ""))
    }, warning = function(war) {
    message(paste("Comparison ", i, ": failed", sep = ""))
    message(war)
    }, error = function(err) {
    message(paste("Comparison ", i, ": failed", sep = ""))
    message(err)
    })
}
