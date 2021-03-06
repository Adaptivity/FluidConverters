package org.cyclops.fluidconverters

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import net.minecraft.client.Minecraft
import net.minecraft.util.{ResourceLocation, IIcon}
import net.minecraftforge.fluids.{FluidRegistry, Fluid}
import org.cyclops.fluidconverters.config.{FluidGroup, FluidGroupRegistry}


/**
 * Analyze the colors of fluids and fluid groups.
 * @author rubensworks
 */
object FluidColorAnalyzer {

    val colors = scala.collection.mutable.Map[Fluid, Int]()
    val groupColors = scala.collection.mutable.Map[FluidGroup, Int]()
    final val DEFAULT = 16777215

    def init() {
        val it = FluidRegistry.getRegisteredFluids.values().iterator()
        while(it.hasNext) {
            val fluid = it.next()
            colors.put(fluid, calculateAverageColor(fluid))
        }

        val it2 = FluidGroupRegistry.getGroups.iterator
        while(it2.hasNext) {
            val group = it2.next()
            groupColors.put(group, calculateAverageColor(group))
        }
    }

    def getAverageColor(fluidGroup: FluidGroup): Int = {
        groupColors.get(fluidGroup) match {
            case Some(i) => i
            case None => DEFAULT
        }
    }

    private def calculateAverageColor(fluidGroup: FluidGroup): Int = {
        var r = 0F
        var g = 0F
        var b = 0F
        for(element <- fluidGroup.getFluidElements) {
            val fluid = element.getFluid
            val color = getAverageColor(fluid)
            r += (-color >> 16 & 0xFF) / 255F
            g += (-color >> 8 & 0xFF) / 255F
            b += (-color & 0xFF) / 255F
        }
        val l = fluidGroup.getFluidElements.length
        r /= l
        g /= l
        b /= l

        (r * 255).toInt << 16 | (g * 255).toInt << 8 | (b * 255).toInt
    }

    def getAverageColor(fluid: Fluid): Int = {
        colors.get(fluid) match {
            case Some(i) => i
            case None => DEFAULT
        }
    }

    private def calculateAverageColor(fluid: Fluid): Int = {
        val default = fluid.getColor

        val block = fluid.getBlock
        if(block == null) {
            return default
        }

        val icon = block.getIcon(0, 0)
        val image = readIcon(icon)

        val width = image.getWidth
        val height = image.getHeight
        val matrix = new Array[Int](width * height)
        image.getRGB(0, 0, width, height, matrix, 0, width)

        if(matrix.length == 0) {
            return default
        }

        var r = 0F
        var g = 0F
        var b = 0F
        matrix.foreach((i) => {
            r += (-i >> 16 & 0xFF) / 255F
            g += (-i >> 8 & 0xFF) / 255F
            b += (-i & 0xFF) / 255F
        })
        r /= matrix.length
        g /= matrix.length
        b /= matrix.length

        (r * 255).toInt << 16 | (g * 255).toInt << 8 | (b * 255).toInt
    }

    private def readIcon(icon: IIcon): BufferedImage = {
        // Determine ResourceLocation params
        val iconName = icon.getIconName
        var iconDomain = "minecraft"
        var iconPath = iconName
        val index = iconPath.indexOf(':')
        if(index >= 0) {
            iconPath = iconName.substring(index + 1, iconName.length)
            if(index > 1) {
                iconDomain = iconName.substring(0, index)
            }
        }
        val resourceLocation = new ResourceLocation(iconDomain.toLowerCase, "textures/blocks/%s.png".format(iconPath))

        // Read the resource location
        val resource = Minecraft.getMinecraft.getResourceManager.getResource(resourceLocation)
        ImageIO.read(resource.getInputStream)
    }

}
