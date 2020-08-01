package net.haesleinhuepf.stardist;


import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import net.haesleinhuepf.clij.CLIJ;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.clij.macro.AbstractCLIJPlugin;
import net.haesleinhuepf.clij.macro.CLIJMacroPlugin;
import net.haesleinhuepf.clij.macro.CLIJOpenCLProcessor;
import net.haesleinhuepf.clij.macro.documentation.OffersDocumentation;
import net.haesleinhuepf.clij2.AbstractCLIJ2Plugin;
import net.haesleinhuepf.clij2.CLIJ2;
import net.haesleinhuepf.clijx.gui.ContinuousWebcamAcquisition;
import net.haesleinhuepf.clijx.plugins.Extrema;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.display.imagej.ImageJFunctions;
import org.scijava.Context;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Plugin;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Plugin(type = CLIJMacroPlugin.class, name = "STARDIST_starDist2DVersatileFluorescentNuclei")
public class StarDist2DVersatileFluorescentNuclei extends AbstractCLIJ2Plugin implements CLIJMacroPlugin, CLIJOpenCLProcessor, OffersDocumentation {

    @Override
    public String getParameterHelpText() {
        return "Image input, ByRef Image destination";
    }

    @Override
    public Object[] getDefaultValues() {
        return new Object[]{};
    }


    @Override
    public boolean executeCL() {
        boolean result = starDist2DVersatileFluorescentNuclei(getCLIJ2(), (ClearCLBuffer) (args[0]), (ClearCLBuffer) (args[1]));
        return result;
    }

    public static boolean starDist2DVersatileFluorescentNuclei(CLIJ2 clij2, ClearCLBuffer input1, ClearCLBuffer output) {
        ImagePlus input = clij2.pull(input1);

        Context context = new Context();
        CommandService command = context.getService(CommandService.class);
        Dataset dataset = new DefaultDataset(context, new ImgPlus<>(ImageJFunctions.convertFloat(input)));

        FutureTask res = (FutureTask) command.run(de.csbdresden.stardist.StarDist2D.class, false,
                "input", dataset,
                "modelChoice", "Versatile (fluorescent nuclei)"
                );

        try {
            System.out.println(((CommandModule)res.get()).getClass().getName());
            DefaultDataset label = (DefaultDataset) ((CommandModule)res.get()).getOutput("label");
            //System.out.println(label.getClass().getName());
            ClearCLBuffer result = clij2.push(label.getImgPlus().getImg());

            clij2.copy(result, output);
            result.close();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //IJ.log(res.getClass().getName());


        return true;
    }

    @Override
    public String getDescription() {
        return "Apply StarDist to an image.";
    }

    @Override
    public String getAvailableForDimensions() {
        return "2D, 3D";
    }

    public static void main(String[] args) {
        new net.imagej.ImageJ().ui().showUI();
        ImagePlus imp = IJ.openImage("C:/structure/data/blobs.tif");

        CLIJ2 clij2 = CLIJ2.getInstance();
        ClearCLBuffer input = clij2.push(imp);

        ClearCLBuffer output = clij2.create(input);

        StarDist2DVersatileFluorescentNuclei.starDist2DVersatileFluorescentNuclei(clij2, input, output);

        clij2.show(output, "output");
    }

}