package funwayguy.bdsandm.network;

import funwayguy.bdsandm.blocks.IStorageBlock;
import funwayguy.bdsandm.client.color.IBdsmColorBlock;
import funwayguy.bdsandm.core.BdsmConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketBdsm implements IMessage
{
	private CompoundNBT tags = new CompoundNBT();
	
	@SuppressWarnings("unused")
	public PacketBdsm()
	{
	}
	
	public PacketBdsm(CompoundNBT tags)
	{
		this.tags = tags;
	}
	
    @Override
    public void fromBytes(ByteBuf buf)
    {
		tags = ByteBufUtils.readTag(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
		ByteBufUtils.writeTag(buf, tags);
    }
	
	public static class ServerHandler implements IMessageHandler<PacketBdsm,PacketBdsm>
	{
        @Override
        public PacketBdsm onMessage(PacketBdsm message, MessageContext ctx)
        {
            int msgType = message.tags.getInteger("msgType");
            
            if(msgType == 1) // Colour change request
            {
                World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.tags.getInteger("dim"));
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                int[] colors = message.tags.getIntArray("color");
                
                BlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof IBdsmColorBlock)
                {
                    ((IBdsmColorBlock)state.getBlock()).setColors(world, state, pos, colors);
                }
            } else if(msgType == 2) // Control Response
            {
                ServerPlayerEntity player = ctx.getServerHandler().player;
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                BlockState state = player.world.getBlockState(pos);
                Direction face = Direction.byIndex(message.tags.getInteger("face"));
                Hand hand = message.tags.getBoolean("offHand") ? Hand.OFF_HAND : Hand.MAIN_HAND;
                boolean isHit = message.tags.getBoolean("isHit");
                boolean altMode = message.tags.getBoolean("altMode");
                int delay = message.tags.getInteger("delay");
                
                if(state.getBlock() instanceof IStorageBlock) ((IStorageBlock)state.getBlock()).onPlayerInteract(player.world, pos, state, face, player, hand, isHit, altMode, delay);
            }
            
            return null;
        }
    }
	
	@SideOnly(Side.CLIENT)
	public static class ClientHandler implements IMessageHandler<PacketBdsm,PacketBdsm>
	{
        @Override
        public PacketBdsm onMessage(PacketBdsm message, MessageContext ctx)
        {
            int msgType = message.tags.getInteger("msgType");
            
            if(msgType == 2) // Control Query
            {
                BlockPos pos = BlockPos.fromLong(message.tags.getLong("pos"));
                PlayerEntity player = Minecraft.getMinecraft().player;
                BlockState state = player.world.getBlockState(pos);
                Vec3d start = player.getPositionEyes(1F);
                Vec3d end = player.getLook(1F);
                end = start.add(end.x * 6D, end.y * 6D, end.z * 6D);
                RayTraceResult rtr = state.getSelectedBoundingBox(player.world, pos).calculateIntercept(start, end);
                Direction face = rtr == null ? Direction.DOWN : rtr.sideHit;
                
                CompoundNBT tagRes = new CompoundNBT();
                tagRes.setInteger("msgType", 2);
                tagRes.setLong("pos", message.tags.getLong("pos"));
                tagRes.setInteger("face", face.getIndex());
                tagRes.setBoolean("isHit", message.tags.getBoolean("isHit"));
                tagRes.setBoolean("offHand", message.tags.getBoolean("offHand"));
                tagRes.setBoolean("altMode", BdsmConfig.altControls);
                tagRes.setInteger("delay", BdsmConfig.dClickDelay);
                return new PacketBdsm(tagRes);
            }
            
            return null;
        }
    }
}
