suppressMessages(require(plotly))
require(crosstalk)
require(htmltools)
require(tidyr)
args <- commandArgs(trailingOnly = TRUE)
#args <- array(data = c("/Users/agastya/Desktop/TPMs For All Sequenced Clones.csv", "/Users/agastya/Desktop/sdf/Ctrl_BvsA_RES.csv", "true"))
data_name <- args[2]
data <- data.frame(read.csv(data_name), row.names = 1)
TPMs <- data.frame(read.csv(args[1]), row.names = 1)
makeAll <- args[3]
compname <- unlist(strsplit(basename(data_name), split = "_RES", fixed = TRUE))[1]
plotsDir <- paste(dirname(data_name), "/Comparison Plots/", sep = "")
if(!dir.exists(plotsDir)){
  dir.create(plotsDir)
} 

data <- subset(data, padj <= 0.1)
data <- subset(data, abs(log2FoldChange) >= 1)
if(nrow(data)<=0){
  stop(paste("no genes found at this significance level for compname", compname))
}
tpm.tall <- TPMs %>% tibble::rownames_to_column(var = "gene_id") %>% gather(key = "clone", value = "TPM", -gene_id)
tpm.tall.deseq <- tpm.tall %>% dplyr::inner_join(data %>% tibble::rownames_to_column(var = "gene_id") %>% select(-baseMean, -lfcSE, -stat, -pvalue))
tpm.tall.deseq$clone <- factor(tpm.tall.deseq$clone, levels = unique(tpm.tall.deseq$clone))
compTitle <- paste("Comparison: ", compname, sep = "")
sd <- crosstalk::SharedData$new(tpm.tall.deseq, ~gene_id, "Select a gene: ")
base <- plot_ly(sd, color = I("slategray"), height = 600) %>%
  group_by(gene_id)
p1 <- base %>%
  summarise(lfcNew = median(log2FoldChange), pvalNew = median(padj))  %>%
  add_markers(x = ~lfcNew, y = ~pvalNew, hoverinfo = "x+y+text", text = ~gene_id) %>%
  layout(
    xaxis = list(title = "Log 2 Fold Change")
  ) %>% layout(yaxis = list(autorange = "reversed")) %>% layout(yaxis = list(type = "log"))

p2 <- base %>%
  add_lines(x = ~clone, y = ~TPM, alpha = 0.3, text = ~gene_id) %>%
  layout(title = compTitle, xaxis = list(title = "Clones"))
plot <- subplot(p1, p2, titleX = TRUE, widths = c(0.2, 0.8)) %>% 
  hide_legend() %>%
  highlight(dynamic = TRUE, selectize = TRUE, color=c('rgba(235,108,47,1','rgba(55,126,184,1)','rgba(77,175,74,1)','rgba(152,78,163,1)'))
workdir <- paste(plotsDir,compname, "/",sep = "")
dir.create(workdir)
setwd(workdir)
htmlName <- paste(compname, ".html", sep = "")
htmlwidgets::saveWidget(as_widget(plot), selfcontained = F, file = htmlName)
if(makeAll == 0){
  browseURL(paste(workdir,"/",htmlName,sep = ""), browser = getOption("browser")) 
}
