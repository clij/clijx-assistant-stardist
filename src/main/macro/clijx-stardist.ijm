
run("CLIJ2 Macro Extensions", "cl_device=");

image = getTitle();
Ext.CLIJ2_push(image);

Ext.STARDIST_starDist2DVersatileFluorescentNuclei(image, result);

Ext.CLIJ2_pull(result);
