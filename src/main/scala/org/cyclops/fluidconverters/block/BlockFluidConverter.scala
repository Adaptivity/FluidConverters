package org.cyclops.fluidconverters.block

import cpw.mods.fml.relauncher.{SideOnly, Side}
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{ItemStack, Item}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{FluidContainerRegistry}
import org.cyclops.fluidconverters.config.FluidGroupRegistry
import org.cyclops.fluidconverters.tileentity.TileEntityFluidConverter

object BlockFluidConverter extends BlockContainer(Material.iron) {
    
    final val NAMEDID = "FluidConverter"

    final val NBTKEY_GROUP = "fluidGroupId"
    final val NBTKEY_SIDE = "fluidSide%s"
    final val NBTKEY_UNITS = "units"
    
    @Override
    def createNewTileEntity(world: World, meta: Int): TileEntity = {
        new TileEntityFluidConverter
    }

    @SideOnly(Side.CLIENT)
    override def getSubBlocks(item: Item, creativeTabs: CreativeTabs, list: java.util.List[_]) {
        val itemList = list.asInstanceOf[java.util.List[ItemStack]]
        for(fluidGroup <- FluidGroupRegistry.getGroups) {
            val groupId = fluidGroup.getGroupId
            val itemStack = new ItemStack(this)
            var tag = itemStack.getTagCompound
            if(tag == null) tag = new NBTTagCompound
            tag.setString(NBTKEY_GROUP, groupId)
            itemStack.setTagCompound(tag)
            itemList.add(itemStack)
        }
    }

    override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack) {
        if(world.getTileEntity(x, y, z) != null) {
            val tile = world.getTileEntity(x, y, z).asInstanceOf[TileEntityFluidConverter]
            if(stack.getTagCompound != null) {
                tile.readConverterDataFromNBT(stack.getTagCompound)
                tile.sendUpdate
            }
        }
        super.onBlockPlacedBy(world, x, y, z, entity, stack)
    }

    override def onBlockActivated(world : World, x : Int, y : Int, z : Int, player : EntityPlayer, side : Int, xPos : Float, yPos : Float, zPos : Float): Boolean = {
        val item = player.getHeldItem
        if(!world.isRemote && item != null && FluidContainerRegistry.isFilledContainer(item)) {
            val fluid = FluidContainerRegistry.getFluidForFilledItem(item).getFluid
            val tile = world.getTileEntity(x, y, z).asInstanceOf[TileEntityFluidConverter]
            if(tile.getFluidGroup.getFluidElement(fluid) != null) {
                tile.setFluid(ForgeDirection.getOrientation(side), fluid)
            }
            return false
        }
        super.onBlockActivated(world, x, y, z, player, side, xPos, yPos, zPos);
    }
    
}