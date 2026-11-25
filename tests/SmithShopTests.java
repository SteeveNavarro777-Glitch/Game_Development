/* FILE PURPOSE: REGRESSION TEST | Blacksmith progression, coin pickups and supplied shop artwork. */
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

final class SmithShopTests {
    private static int checks;
    private SmithShopTests(){}

    public static void main(String[] args) throws Exception {
        Assets.load();
        System.out.println("PASS: "+run()+" smith/economy checks");
    }

    static int run() throws Exception {
        Player player=new Player(World.SPAWN_X,World.SPAWN_Y);
        EquipmentLoadout gear=player.equipment();
        check(gear.level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.BASIC,"Spear does not begin Basic");
        check(gear.level(EquipmentLoadout.Gear.SHIELD)==EquipmentLoadout.Level.BASIC,"Shield does not begin Basic");
        check(gear.ownedLevel(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.BASIC,"Spear ownership does not begin Basic");
        check(!gear.canUpgrade(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.LEGENDARY),"Legendary spear skips Rare prerequisite");
        check(!gear.equip(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.LEGENDARY),"Unowned Legendary spear equipped");
        check(gear.spearDamage(1)==1,"Basic spear damage changed");
        check(gear.guardThreshold()==.5&&gear.guardMoveMultiplier()==.4,"Basic shield tuning changed");

        player.addCoins(105);
        SmithShop shop=new SmithShop();shop.open();
        shop.select(1);check(!shop.purchase(player)&&player.coins()==105,"Locked Legendary spear charged coins");
        shop.select(0);check(shop.purchase(player)&&player.coins()==85,"Rare spear purchase failed");
        check(gear.owns(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.RARE)&&gear.level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.BASIC,
                "Rare spear purchase auto-equipped instead of entering inventory");
        check(gear.spearDamage(1)==1,"Unequipped Rare spear changed damage");
        check(shop.message().contains("INVENTORY")&&player.itemMessage().toLowerCase().contains("inventory"),"Purchase omitted inventory equip guidance");
        shop.select(1);check(shop.purchase(player)&&player.coins()==50,"Legendary spear purchase failed");
        check(gear.ownedLevel(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.LEGENDARY&&gear.level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.BASIC,
                "Legendary spear purchase auto-equipped");
        check(player.equipGear(0,Inventory.Item.SPEAR,EquipmentLoadout.Level.RARE)&&gear.spearDamage(1)==2,"Owned Rare spear did not equip from inventory");
        check(player.equipGear(0,Inventory.Item.SPEAR,EquipmentLoadout.Level.LEGENDARY)&&gear.spearDamage(1)==3,"Owned Legendary spear did not equip from inventory");
        check(player.inventory().count(Inventory.Item.SPEAR)==1,"Equipping a spear tier duplicated the base inventory item");
        shop.select(2);check(shop.purchase(player)&&player.coins()==32,"Rare shield purchase failed");
        check(gear.guardThreshold()==.5&&gear.guardMoveMultiplier()==.4,"Unequipped Rare shield changed guard tuning");
        check(player.equipGear(1,Inventory.Item.SHIELD,EquipmentLoadout.Level.RARE)&&gear.guardThreshold()==0&&gear.guardMoveMultiplier()==.5,
                "Owned Rare shield did not equip from inventory");
        shop.select(3);check(shop.purchase(player)&&player.coins()==0,"Legendary shield purchase failed");
        check(gear.guardThreshold()==0&&gear.level(EquipmentLoadout.Gear.SHIELD)==EquipmentLoadout.Level.RARE,"Legendary shield purchase auto-equipped");
        check(player.equipGear(1,Inventory.Item.SHIELD,EquipmentLoadout.Level.LEGENDARY)&&gear.guardThreshold()==-.5&&gear.guardMoveMultiplier()==.6,
                "Owned Legendary shield did not equip from inventory");
        check(player.inventory().count(Inventory.Item.SHIELD)==1,"Equipping a shield tier duplicated the base inventory item");
        check(!shop.purchase(player)&&player.coins()==0,"Owned upgrade charged twice");
        check(ShieldGuard.faces(Player.Dir.RIGHT,-4,9,gear.guardThreshold()),"Legendary shield did not protect its wider rear-side arc");
        check(!ShieldGuard.faces(Player.Dir.RIGHT,-10,0),"Basic shield unexpectedly protects directly behind");
        check(shop.cardAt(new Point(60,100))==0&&shop.cardAt(new Point(10,10))==-1,"Shop card hit testing failed");

        Player collector=new Player(100,100);List<CoinDrop> drops=CoinDrop.scatter(100,100,13);
        int total=0;for(CoinDrop drop:drops)total+=drop.value();
        check(total==13&&drops.size()<=5,"Coin reward partition changed its value or bound");
        for(int tick=0;tick<180;tick++)for(CoinDrop drop:drops)drop.update(1.0/60,collector);
        check(collector.coins()==13,"Coin bundles did not credit the wallet exactly once");
        for(CoinDrop drop:drops)drop.update(1,collector);
        check(collector.coins()==13,"Collected coins credited twice");

        SmithShopArt art=new SmithShopArt();
        for(EquipmentLoadout.Gear item:EquipmentLoadout.Gear.values())for(EquipmentLoadout.Level level:new EquipmentLoadout.Level[]{EquipmentLoadout.Level.RARE,EquipmentLoadout.Level.LEGENDARY}){
            BufferedImage icon=art.icon(item,level);int opaque=0;
            for(int y=0;y<icon.getHeight();y++)for(int x=0;x<icon.getWidth();x++)if((icon.getRGB(x,y)>>>24)>40)opaque++;
            check(icon.getWidth()==58&&icon.getHeight()==58&&opaque>80,"Supplied shop icon is missing or badly cropped");
            BufferedImage shared=EquipmentArt.icon(item,level);
            check(shared==icon||shared.getWidth()==58&&shared.getHeight()==58,"Inventory did not reuse supplied smith art");
        }
        check(EquipmentArt.spear(EquipmentLoadout.Level.RARE).getHeight()>=50&&
                EquipmentArt.spear(EquipmentLoadout.Level.LEGENDARY).getHeight()>=50,"Purchased spear art was not prepared for Agyu");
        check(EquipmentArt.shield(EquipmentLoadout.Level.RARE).getHeight()>=25&&
                EquipmentArt.shield(EquipmentLoadout.Level.LEGENDARY).getHeight()>=25,"Purchased shield art was not prepared for Agyu");

        EquipmentLoadout.Level legendary=EquipmentLoadout.Level.LEGENDARY;
        for(Player.Dir dir:Player.Dir.values()){
            BufferedImage idle=HeldItemArt.frame(Inventory.Item.SPEAR,dir,0);
            BufferedImage guard=HeldItemArt.guardFrame(Inventory.Item.SHIELD,dir,0,false);
            checkReplacedSpear(idle,EquipmentArt.carried(idle,dir,legendary,legendary,false),dir+" carry");
            checkReplacedSpear(guard,EquipmentArt.carried(guard,dir,legendary,legendary,true),dir+" guard");
        }

        BufferedImage effectPreview=new BufferedImage(800,260,BufferedImage.TYPE_INT_RGB);Graphics2D effectGraphics=effectPreview.createGraphics();
        try{
            effectGraphics.setColor(new java.awt.Color(0x15221F));effectGraphics.fillRect(0,0,800,260);
            effectGraphics.setFont(new java.awt.Font("Monospaced",java.awt.Font.BOLD,11));
            EquipmentLoadout.Level[] effectLevels={EquipmentLoadout.Level.RARE,EquipmentLoadout.Level.LEGENDARY};
            for(int tier=0;tier<effectLevels.length;tier++){
                EquipmentLoadout.Level level=effectLevels[tier];
                int spearY=45+tier*125,shieldY=96+tier*125;
                effectGraphics.setColor(java.awt.Color.WHITE);effectGraphics.drawString(level.label.toUpperCase()+" SPEAR",8,spearY);
                effectGraphics.drawString(level.label.toUpperCase()+" SHIELD",8,shieldY);
                for(int frame=0;frame<8;frame++){
                    BufferedImage spearEffect=EquipmentArt.spearEffect(level,frame);
                    BufferedImage shieldEffect=EquipmentArt.shieldEffect(level,frame);
                    check(hasVisiblePixels(spearEffect,8),level+" spear effect frame "+frame+" is empty");
                    check(hasVisiblePixels(shieldEffect,8),level+" shield effect frame "+frame+" is empty");
                    int x=170+frame*78;
                    effectGraphics.drawImage(spearEffect,x-spearEffect.getWidth()/2,spearY-spearEffect.getHeight()/2,null);
                    effectGraphics.drawImage(shieldEffect,x-shieldEffect.getWidth()/2,shieldY-shieldEffect.getHeight()/2,null);
                }
            }
        }finally{effectGraphics.dispose();}
        File effectOutput=new File("build/preview-equipment-effects.png");effectOutput.getParentFile().mkdirs();
        ImageIO.write(effectPreview,"png",effectOutput);check(effectOutput.isFile(),"Equipment effect preview was not written");

        InventoryScreen inventoryScreen=new InventoryScreen();inventoryScreen.open(player);
        check(inventoryScreen.entryCount()==9,"Inventory does not expose all six gear tiers and three potions");
        int rareSpear=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.RARE);
        int legendarySpear=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.LEGENDARY);
        int rareShield=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.RARE);
        int legendaryShield=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.LEGENDARY);
        check(inventoryScreen.entryOwned(player,rareSpear)&&inventoryScreen.entryOwned(player,legendarySpear)
                &&inventoryScreen.entryOwned(player,rareShield)&&inventoryScreen.entryOwned(player,legendaryShield),
                "Purchased smith gear is missing from the explicit inventory cards");
        inventoryScreen.selectEntry(legendarySpear);
        check(inventoryScreen.item()==Inventory.Item.SPEAR&&inventoryScreen.selectedLevel()==EquipmentLoadout.Level.LEGENDARY
                &&player.equipGear(0,inventoryScreen.item(),inventoryScreen.selectedLevel())
                &&gear.level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.LEGENDARY,
                "Legendary spear card did not replace Agyu's equipped spear");
        inventoryScreen.selectEntry(legendaryShield);
        check(inventoryScreen.item()==Inventory.Item.SHIELD&&inventoryScreen.selectedLevel()==EquipmentLoadout.Level.LEGENDARY
                &&player.equipGear(1,inventoryScreen.item(),inventoryScreen.selectedLevel())
                &&gear.level(EquipmentLoadout.Gear.SHIELD)==EquipmentLoadout.Level.LEGENDARY,
                "Legendary shield card did not replace Agyu's equipped shield");
        inventoryScreen.select(Inventory.Item.SPEAR.ordinal());inventoryScreen.selectLevel(EquipmentLoadout.Level.BASIC.ordinal());
        check(inventoryScreen.selectedLevel()==EquipmentLoadout.Level.BASIC&&player.equipGear(0,Inventory.Item.SPEAR,inventoryScreen.selectedLevel()),
                "Inventory tier selection could not re-equip Basic spear");
        check(gear.level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.BASIC&&gear.ownedLevel(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.LEGENDARY,
                "Re-equipping Basic erased purchased spear ownership");
        BufferedImage equipmentPreview=new BufferedImage(480,260,BufferedImage.TYPE_INT_RGB);Graphics2D equipmentGraphics=equipmentPreview.createGraphics();
        try{
            equipmentGraphics.setColor(new java.awt.Color(0x263B35));equipmentGraphics.fillRect(0,0,480,260);
            equipmentGraphics.setFont(new java.awt.Font("Monospaced",java.awt.Font.BOLD,12));
            for(EquipmentLoadout.Level level:EquipmentLoadout.Level.values()){
                int x=80+level.ordinal()*160;equipmentGraphics.setColor(java.awt.Color.WHITE);
                equipmentGraphics.drawString(level.label.toUpperCase(),x-34,18);
                HeldItemArt.draw(equipmentGraphics,x,118,Inventory.Item.SPEAR,Player.Dir.RIGHT,0,level,level);
                HeldItemArt.drawGuard(equipmentGraphics,x,238,Inventory.Item.SHIELD,Player.Dir.DOWN,0,false,level,level);
            }
        }finally{equipmentGraphics.dispose();}
        File equipmentOutput=new File("build/preview-equipment-tiers.png");equipmentOutput.getParentFile().mkdirs();
        ImageIO.write(equipmentPreview,"png",equipmentOutput);check(equipmentOutput.isFile(),"Equipment tier preview was not written");

        BufferedImage directionPreview=new BufferedImage(640,380,BufferedImage.TYPE_INT_RGB);Graphics2D directionGraphics=directionPreview.createGraphics();
        try{
            directionGraphics.setColor(new java.awt.Color(0x263B35));directionGraphics.fillRect(0,0,640,380);
            directionGraphics.setFont(new java.awt.Font("Monospaced",java.awt.Font.BOLD,11));
            for(Player.Dir dir:Player.Dir.values()){
                int x=80+dir.ordinal()*160;directionGraphics.setColor(java.awt.Color.WHITE);
                directionGraphics.drawString(dir.name(),x-18,18);
                HeldItemArt.drawGuard(directionGraphics,x,122,Inventory.Item.SHIELD,dir,0,false,
                        EquipmentLoadout.Level.RARE,EquipmentLoadout.Level.RARE);
                HeldItemArt.drawGuard(directionGraphics,x,246,Inventory.Item.SHIELD,dir,0,false,
                        EquipmentLoadout.Level.LEGENDARY,EquipmentLoadout.Level.LEGENDARY);
                HeldItemArt.draw(directionGraphics,x,366,Inventory.Item.SPEAR,dir,0,
                        EquipmentLoadout.Level.LEGENDARY,EquipmentLoadout.Level.LEGENDARY);
            }
        }finally{directionGraphics.dispose();}
        File directionOutput=new File("build/preview-equipment-directions.png");
        ImageIO.write(directionPreview,"png",directionOutput);check(directionOutput.isFile(),"Directional equipment preview was not written");

        inventoryScreen.select(Inventory.Item.SPEAR.ordinal());inventoryScreen.selectLevel(EquipmentLoadout.Level.LEGENDARY.ordinal());
        BufferedImage inventoryPreview=new BufferedImage(480,360,BufferedImage.TYPE_INT_RGB);Graphics2D inventoryGraphics=inventoryPreview.createGraphics();
        try{inventoryScreen.draw(inventoryGraphics,player);}finally{inventoryGraphics.dispose();}
        File inventoryOutput=new File("build/preview-inventory-equipment.png");
        ImageIO.write(inventoryPreview,"png",inventoryOutput);check(inventoryOutput.isFile(),"Equipment inventory preview was not written");

        BufferedImage preview=new BufferedImage(480,320,BufferedImage.TYPE_INT_RGB);Graphics2D g=preview.createGraphics();
        try{g.setColor(new java.awt.Color(0x41634A));g.fillRect(0,0,480,320);shop.draw(g,player);}finally{g.dispose();}
        File output=new File("build/preview-smith-shop.png");output.getParentFile().mkdirs();ImageIO.write(preview,"png",output);
        check(output.isFile(),"Smith shop preview was not written");
        return checks;
    }

    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}

    // Count native blue pixels that would reveal the starter spear under a golden replacement.
    private static void checkReplacedSpear(BufferedImage original,BufferedImage upgraded,String pose){
        int metal=0,remaining=0;
        for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++){
            int pixel=original.getRGB(x,y),r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
            if((pixel>>>24)>160&&g>90&&b>95&&g>r*1.5){
                metal++;
                if(upgraded.getRGB(x,y)==pixel)remaining++;
            }
        }
        check(metal>8&&remaining*5<metal,"Starter spear remains visible during "+pose);
    }

    private static boolean hasVisiblePixels(BufferedImage image,int minimum){
        if(image==null)return false;int visible=0;
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>48&&++visible>=minimum)return true;
        return false;
    }
}
