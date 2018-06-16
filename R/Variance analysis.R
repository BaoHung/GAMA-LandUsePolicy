dataexf <- read.table("resVariance.csv",header=T,sep=",",dec=".")
dataexf
summary(dataexf)
aovexf <- aov(rate_same_landuse_2010~weight_investment*weight_cost*weight_delay,data=dataexf)
aovexf
summary(aovexf)
summary(aovexf)[[1]][2]
round(summary(aovexf)[[1]][2]/sum(summary(aovexf)[[1]][2])*100,2)
