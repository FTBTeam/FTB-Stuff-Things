package dev.ftb.mods.ftbstuffnthings.client.model;

import dev.ftb.mods.ftbstuffnthings.blocks.tube.TubeBlockEntity;
import dev.ftb.mods.ftbstuffnthings.util.DirectionUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TubeModel extends DelegateBlockStateModel {
    private static final Map<Integer, List<BlockStateModelPart>> MODEL_CACHE = new ConcurrentHashMap<>();

    private final BlockStateModel[] rotated;

    public TubeModel(BlockStateModel centre, BlockStateModel[] rotated) {
        super(centre);
        this.rotated = rotated;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        super.collectParts(level, pos, state, random, parts);

        ModelData extraData = level.getModelData(pos);
        Integer connected = extraData.get(TubeBlockEntity.CONNECTION_PROPERTY);

        if (connected != null) {
            List<BlockStateModelPart> cachedParts = MODEL_CACHE.get(connected);
            if (cachedParts == null) {
                cachedParts = new ArrayList<>();
                for (Direction dir : DirectionUtil.VALUES) {
                    if (DirectionUtil.getDirectionBit(connected, dir)) {
                        rotated[dir.get3DDataValue()].collectParts(level, pos, state, random, cachedParts);
                    }
                }
                MODEL_CACHE.put(connected, cachedParts);
            }
            parts.addAll(cachedParts);
        }
    }

    // TODO port this fully to 26.1 if we add tubes back

//    public record Geometry(BlockModel centre, BlockModel tubePart) implements UnbakedModel {
//        private static final Vector3f BLOCK_CENTER = new Vector3f(0.5f, 0.5f, 0.5f);
//
//        // JSON models for the tube arm is in the DOWN orientation
//        // rotate as appropriate to get a rotated model for each direction (DUNSWE order)
//        private static final BlockModelRotation[] ROTATIONS = new BlockModelRotation[] {
//                BlockModelRotation.X0_Y0,
//                BlockModelRotation.X180_Y0,
//                BlockModelRotation.X270_Y0,
//                BlockModelRotation.X270_Y180,
//                BlockModelRotation.X270_Y270,
//                BlockModelRotation.X270_Y90
//        };
//
//        @Override
//        public BakedModel bake(IGeometryBakingContext iGeometryBakingContext, ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState, ItemOverrides itemOverrides) {
//            BakedModel[] rotated = new BakedModel[6];
//
//            for (Direction dir : DirectionUtil.VALUES) {
//                int d = dir.get3DDataValue();
//                ModelState rotatedState = UnbakedGeometryHelper.composeRootTransformIntoModelState(
//                        modelState,
//                        ROTATIONS[d].getRotation().applyOrigin(BLOCK_CENTER)
//                );
//                rotated[d] = tubePart.bake(modelBaker, tubePart, function, rotatedState, true);
//            }
//
//            return new TubeModel(centre.bake(modelBaker, centre, function, modelState, true), rotated);
//        }
//    }
//
//    public enum Loader implements UnbakedModelLoader<Geometry> {
//        INSTANCE;
//
//        public static final Identifier ID = FTBStuffNThings.id("tube");
//
//        @Override
//        public Geometry read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//            BlockModel centre = loadModel(FTBStuffNThings.id("block/tube_center"));
//            BlockModel tubePart = loadModel(FTBStuffNThings.id("block/tube_base"));
//
//            return new Geometry(centre, tubePart);
//        }
//
//        private static BlockModel loadModel(Identifier location) {
//            ResourceManager manager = Minecraft.getInstance().getResourceManager();
//            Identifier file = ModelBakery.MODEL_LISTER.idToFile(location);
//            try (InputStream stream = manager.getResourceOrThrow(file).open()) {
//                return net.minecraft.client.renderer.block.model.BlockModel.Unbaked.fromStream(new InputStreamReader(stream));
//            } catch (IOException e) {
//                throw new JsonParseException("Failed to load part model '" + file + "'", e);
//            }
//        }
//    }
}
