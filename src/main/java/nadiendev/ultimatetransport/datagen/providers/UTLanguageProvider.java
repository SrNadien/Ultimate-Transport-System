package nadiendev.ultimatetransport.datagen.providers;

import nadiendev.ultimatetransport.UltimateTransport;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public abstract class UTLanguageProvider extends LanguageProvider {

    protected UTLanguageProvider(PackOutput output, String locale) {
        super(output, UltimateTransport.MODID, locale);
    }

    public static class English extends UTLanguageProvider {

        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.ultimatetransport", "Ultimate Transport System");

            add("block.ultimatetransport.energy_cable", "Energy Cable");
            add("block.ultimatetransport.fluid_cable", "Fluid Cable");
            add("block.ultimatetransport.item_cable", "Item Cable");
            add("block.ultimatetransport.gas_cable", "Gas Cable");
            add("block.ultimatetransport.source_cable", "Mana Cable");
            add("block.ultimatetransport.universal_cable", "Universal Cable");
            add("block.ultimatetransport.tube", "Transport Tube");
            add("block.ultimatetransport.station", "Tube Station");
            add("block.ultimatetransport.station_horizontal", "Tube Station");
            add("item.ultimatetransport.tube_down", "Transport Tube (Down)");
            add("item.ultimatetransport.tube_up", "Transport Tube (Up)");
            add("item.ultimatetransport.tube_north", "Transport Tube (North)");
            add("item.ultimatetransport.tube_south", "Transport Tube (South)");
            add("item.ultimatetransport.tube_east", "Transport Tube (East)");
            add("item.ultimatetransport.tube_west", "Transport Tube (West)");
            add("tooltip.ultimatetransport.direction", "Direction: %s");
            add("tooltip.ultimatetransport.direction.0", "DOWN");
            add("tooltip.ultimatetransport.direction.1", "UP");
            add("tooltip.ultimatetransport.direction.2", "NORTH");
            add("tooltip.ultimatetransport.direction.3", "SOUTH");
            add("tooltip.ultimatetransport.direction.4", "EAST");
            add("tooltip.ultimatetransport.direction.5", "WEST");
            add("tooltip.ultimatetransport.autodirection.1", "Faces the side you place it on");
            add("tooltip.ultimatetransport.autodirection.2", "Place on a block's north face for a north tube");
            add("tooltip.ultimatetransport.autodirection.3", "Craft it to lock a direction instead");
            add("ultimatetransport.configuration.max_tube_speed", "Max tube speed");
            add("ultimatetransport.configuration.suppress_walk", "Silence footsteps while riding");
            add("ultimatetransport.configuration.render_pass", "Tube render pass");
            add("ultimatetransport.configuration.lenient_culling", "Merge touching tubes");
            add("ultimatetransport.configuration.placement_preview", "Show placement preview");

            add("item.ultimatetransport.basic_upgrade", "Basic Upgrade");
            add("item.ultimatetransport.improved_upgrade", "Improved Upgrade");
            add("item.ultimatetransport.advanced_upgrade", "Advanced Upgrade");
            add("item.ultimatetransport.ultimate_upgrade", "Ultimate Upgrade");
            add("item.ultimatetransport.infinity_upgrade", "Infinity Upgrade");
            add("item.ultimatetransport.configurator", "Configurator");
            add("item.ultimatetransport.facade", "Facade");
            add("item.ultimatetransport.facade.of", "%s Facade");
            add("tooltip.ultimatetransport.facade.blank", "Use on a full block to copy it.");
            add("tooltip.ultimatetransport.facade.filled", "Use on a cable face to cover it. The configurator takes it back off.");
            String[] batteries = {"Basic", "Advanced", "Reinforced", "Elite", "Industrial", "Ultimate",
                    "Quantum", "Stellar", "Cosmic", "Titanic", "Infinite"};
            for (int tier = 1; tier <= batteries.length; tier++) {
                add("block.ultimatetransport.energy_cell_" + tier, batteries[tier - 1] + " Battery");
            }
            add("block.ultimatetransport.creative_energy_cell", "Creative Battery");
            add("screen.ultimatetransport.cell_endless", "Endless");
            add("tooltip.ultimatetransport.cell_charge_slot", "Charge: the cell fills the item here.");
            add("tooltip.ultimatetransport.cell_discharge_slot", "Discharge: the item empties into the cell here.");

            add("ultimatetransport.cell_side.disabled", "Disabled");
            add("ultimatetransport.cell_side.input", "Input");
            add("ultimatetransport.cell_side.output", "Output");
            add("ultimatetransport.cell_side.both", "Input and output");
            add("ultimatetransport.cell_display.none", "Nothing");
            add("ultimatetransport.cell_display.bar", "Charge bar");
            add("ultimatetransport.cell_display.io", "Input and output");
            add("message.ultimatetransport.cell_display", "%s: %s");

            add("message.ultimatetransport.cell_charge", "%s / %s FE");
            add("tooltip.ultimatetransport.cell_charge", "%s / %s FE");
            add("tooltip.ultimatetransport.cell_transfer", "%s FE/t");

            add("ultimatetransport.transfer_type.energy", "Energy");
            add("ultimatetransport.transfer_type.fluid", "Fluid");
            add("ultimatetransport.transfer_type.item", "Items");
            add("ultimatetransport.transfer_type.gas", "Gas");
            add("ultimatetransport.transfer_type.source", "Source");
            add("ultimatetransport.transfer_type.universal", "Everything");

            add("ultimatetransport.connection_mode.none", "Disconnected");
            add("ultimatetransport.connection_mode.insert", "Insert");
            add("ultimatetransport.connection_mode.extract", "Extract");

            add("ultimatetransport.redstone_mode.ignored", "Redstone ignored");
            add("ultimatetransport.redstone_mode.off_when_powered", "Off when powered");
            add("ultimatetransport.redstone_mode.on_when_powered", "On when powered");
            add("ultimatetransport.redstone_mode.always_off", "Always off");

            add("ultimatetransport.distribution_mode.round_robin", "Round robin");
            add("ultimatetransport.distribution_mode.nearest", "Nearest first");
            add("ultimatetransport.distribution_mode.furthest", "Furthest first");
            add("ultimatetransport.distribution_mode.random", "Random");

            add("ultimatetransport.upgrade.none", "No upgrade");
            add("ultimatetransport.upgrade.basic", "Basic");
            add("ultimatetransport.upgrade.improved", "Improved");
            add("ultimatetransport.upgrade.advanced", "Advanced");
            add("ultimatetransport.upgrade.ultimate", "Ultimate");
            add("ultimatetransport.upgrade.infinity", "Infinity");

            add("ultimatetransport.direction.down", "Bottom");
            add("ultimatetransport.direction.up", "Top");
            add("ultimatetransport.direction.north", "North");
            add("ultimatetransport.direction.south", "South");
            add("ultimatetransport.direction.west", "West");
            add("ultimatetransport.direction.east", "East");

            add("message.ultimatetransport.side_mode", "%s: %s");

            add("screen.ultimatetransport.cable", "%s - %s");
            add("screen.ultimatetransport.filter", "Filter rule");
            add("screen.ultimatetransport.priority", "Priority: %s");
            add("screen.ultimatetransport.upgrade_slot", "Upgrade");
            add("screen.ultimatetransport.filter.add", "Add");
            add("screen.ultimatetransport.filter.search", "Search");
            add("tooltip.ultimatetransport.theme", "Screen theme: %s");
            add("ultimatetransport.theme.dark", "Dark");
            add("ultimatetransport.theme.light", "Light");
            add("tooltip.ultimatetransport.retrieve", "Direction: %s");
            add("tooltip.ultimatetransport.retrieve.hint", "Retrieve pulls from the network into this block.");
            add("ultimatetransport.retrieve.off", "Extract");
            add("ultimatetransport.retrieve.on", "Retrieve");
            add("ultimatetransport.link.severed", "Disconnected");
            add("ultimatetransport.link.joined", "Connected");
            add("screen.ultimatetransport.filter.edit", "Edit");
            add("screen.ultimatetransport.filter.remove", "Remove");
            add("screen.ultimatetransport.filter.cancel", "Cancel");
            add("screen.ultimatetransport.filter.submit", "Submit");
            add("screen.ultimatetransport.filter.item_tag", "Item/Tag");
            add("screen.ultimatetransport.filter.nbt", "NBT data");
            add("screen.ultimatetransport.filter.destination", "Destination");
            add("screen.ultimatetransport.filter.destination.any", "Anywhere");
            add("screen.ultimatetransport.filter.any", "anything");
            add("screen.ultimatetransport.cell_side", "%s: %s");
            add("ultimatetransport.filter_mode.whitelist", "Whitelist");
            add("ultimatetransport.filter_mode.blacklist", "Blacklist");

            add("tooltip.ultimatetransport.unlimited", "Unlimited");
            add("tooltip.ultimatetransport.upgrade.energy", "%s FE/t");
            add("tooltip.ultimatetransport.upgrade.fluid", "%s mB/t");
            add("tooltip.ultimatetransport.upgrade.item", "%s items every %s ticks");
            add("tooltip.ultimatetransport.upgrade.item_tick", "%s items every tick");
            add("tooltip.ultimatetransport.upgrade.gas", "%s units/t");
            add("tooltip.ultimatetransport.upgrade.redstone_yes", "Redstone mode");
            add("tooltip.ultimatetransport.upgrade.redstone_no", "No redstone mode");
            add("tooltip.ultimatetransport.upgrade.distribution_yes", "Distribution mode");
            add("tooltip.ultimatetransport.upgrade.distribution_no", "No distribution mode");
            add("tooltip.ultimatetransport.upgrade.filter_yes", "Filter rules");
            add("tooltip.ultimatetransport.upgrade.filter_no", "No filter rules");
            add("tooltip.ultimatetransport.upgrade.install", "Sneak and right click a cable face to fit it");
            add("tooltip.ultimatetransport.upgrade.rules", "%s filter rules (%s), kept on this upgrade");
            add("tooltip.ultimatetransport.configurator", "Click a face to step it, sneak-click to step it back.");
            add("tooltip.ultimatetransport.configurator.sneak", "Sneak and scroll to change mode, or press the mode key for the wheel.");
            add("tooltip.ultimatetransport.configurator.mode", "Mode: %s");
            add("message.ultimatetransport.configurator_mode", "Configurator: %s");
            add("ultimatetransport.configurator_mode.configurate_energy", "Configure: Energy");
            add("ultimatetransport.configurator_mode.configurate_fluid", "Configure: Fluid");
            add("ultimatetransport.configurator_mode.configurate_item", "Configure: Items");
            add("ultimatetransport.configurator_mode.configurate_gas", "Configure: Chemicals");
            add("ultimatetransport.configurator_mode.configurate_source", "Configure: Source");
            add("ultimatetransport.configurator_mode.empty", "Empty");
            add("ultimatetransport.configurator_mode.rotate", "Rotate");
            add("ultimatetransport.configurator_mode.wrench", "Wrench");
            add("key.categories.ultimatetransport", "Ultimate Transport");
            add("key.ultimatetransport.configurator_menu", "Configurator modes");
            add("message.ultimatetransport.configurator_unsupported", "This block has no %s to configure");
            add("ultimatetransport.face.top", "Top");
            add("ultimatetransport.face.bottom", "Bottom");
            add("ultimatetransport.face.front", "Front");
            add("ultimatetransport.face.back", "Back");
            add("ultimatetransport.face.left", "Left");
            add("ultimatetransport.face.right", "Right");
            add("screen.ultimatetransport.side_config.hint", "Left-click steps forward, right-click steps back.");
            add("screen.ultimatetransport.side_config.tab", "Side Configuration");
            add("tooltip.ultimatetransport.cell_bank", "Bank of %s batteries");
            add("screen.ultimatetransport.side_config.eject", "Auto-eject");
            add("screen.ultimatetransport.side_config.eject_on", "Pushing into neighbours");
            add("screen.ultimatetransport.side_config.eject_off", "Holding everything in");
            add("screen.ultimatetransport.side_config.close", "Close");
            add("screen.ultimatetransport.side_config.clear", "Clear every face");
            add("screen.ultimatetransport.side_config.unlinked", "Nothing to configure on this face");
            add("config.ultimatetransport.gui_theme", "Screen theme");

            add("block.ultimatetransport.fuel_generator", "Fuel Generator");
            add("block.ultimatetransport.lava_generator", "Lava Generator");
            add("block.ultimatetransport.solar_generator", "Solar Generator");
            add("block.ultimatetransport.nether_star_generator", "Nether Star Generator");
            add("jei.ultimatetransport.fuel.rate", "%s FE/t");
            add("jei.ultimatetransport.fuel.duration", "%s");
            add("jei.ultimatetransport.fuel.ticks", "%s ticks");
            add("jei.ultimatetransport.generator.fuel_generator", "Burns anything a furnace would, for exactly as long as a furnace would burn it. Feed it by hand, by hopper or by item cable, and it pushes its buffer into anything next to it that takes power.");
            add("jei.ultimatetransport.generator.lava_generator", "Drinks a bucket of lava at a time. The bucket comes back empty in the slot, so an item cable can take it away and bring it back full.");
            add("jei.ultimatetransport.generator.solar_generator", "Runs while it is daytime, not raining, and the block above it can see the sky. No fuel slot, because it eats nothing.");
            add("jei.ultimatetransport.generator.nether_star_generator", "One nether star runs it for 20,000 ticks at 2,000 FE/t. The end of the ladder, and the only generator that will keep a bank of ultimate cables fed on its own.");
            add("screen.ultimatetransport.generator_running", "Running: %s FE/t");
            add("screen.ultimatetransport.generator_idle", "Idle");
            add("screen.ultimatetransport.generator_output", "Output: %s FE/t");

            add("item.ultimatetransport.destination_tool", "Destination Tool");
            add("tooltip.ultimatetransport.destination_tool", "Right-click a block face to record it, then drop it on a rule's destination slot.");
            add("message.ultimatetransport.destination", "%s, %s, %s (%s)");

            add("tooltip.ultimatetransport.redstone_mode", "Redstone: %s");
            add("tooltip.ultimatetransport.distribution", "Distribution: %s");
            add("tooltip.ultimatetransport.filter_mode", "Filter: %s");
            add("tooltip.ultimatetransport.filter.pick", "Click with an item to fill the rule from it. Sneak-click to clear.");
            add("tooltip.ultimatetransport.filter.item_tag", "An id like minecraft:iron_ingot, or #c:ingots for a tag.");
            add("tooltip.ultimatetransport.filter.nbt", "Optional NBT to narrow the match. Leave empty to match any.");
            add("tooltip.ultimatetransport.filter.nbt.exact", "NBT must match exactly");
            add("tooltip.ultimatetransport.filter.nbt.partial", "NBT only has to contain this");
            add("tooltip.ultimatetransport.filter.inverted", "Inverted: matches everything but this");
            add("tooltip.ultimatetransport.filter.not_inverted", "Matches this");
            add("tooltip.ultimatetransport.filter.destination", "Click with the Destination Tool to send what this rule matches to one place only.");
            add("tooltip.ultimatetransport.filter.destination.clear", "Click with an empty hand to clear it.");

            add("config.jade.plugin_ultimatetransport.cable", "Cable face");
            add("config.jade.plugin_ultimatetransport.energy_cell", "Energy cell");
            add("config.jade.plugin_ultimatetransport.tube", "Transport tube");
            add("jade.ultimatetransport.tube_direction", "Carries: %s");
            add("jade.ultimatetransport.rules", "%s, %s rules");
            add("jade.ultimatetransport.facade", "Facade: %s");
            add("jade.ultimatetransport.travelling", "%s travelling");

            add("jei.ultimatetransport.cable", "Cables move their cargo instantly along a network. Configure each face with the Configurator, or right-click it with an empty hand for modes, an upgrade slot and filter rules.");
            add("jei.ultimatetransport.upgrade", "Slots into one cable face. It raises the throughput of that face and is what lets it use redstone modes, distribution modes and filter rules at all.");
            add("jei.ultimatetransport.configurator", "Right-click a cable face that touches a machine to flip it between insert and extract; the rest of a run has nothing to configure. On an energy cell it cycles the face between input, output, both and disabled,. Sneak-use in the air to change tool mode.");
            add("jei.ultimatetransport.facade", "Use on any full block to copy it, then on a cable face to cover it. The cover is a two pixel panel with a hole for any arm passing through. The Configurator takes it back off.");
            add("jei.ultimatetransport.destination_tool", "Right-click a block face to record it, then drop the tool on a rule's destination slot. Whatever that rule matches then goes to that face and nowhere else.");
            add("jei.ultimatetransport.player_tube", "A directional glass tube. Anything that walks in accelerates along the tube axis until it hits the speed cap, so long runs are fast and short hops are gentle. Fall damage is cancelled inside, so a vertical shaft can be as deep as you like.");
            add("jei.ultimatetransport.station", "Two blocks tall and hollow: where you get in and out. Standing on a station over an upward tube lifts you into the network. Sneak to stop at an intermediate station instead of riding to the end.");
            add("jei.ultimatetransport.energy_cell", "Stores energy and pushes it into whatever is next to it, but never into another cell that is fuller, so a bank levels out. Keeps its charge when broken and works from your inventory.");
        }
    }

    public static class Spanish extends UTLanguageProvider {

        public Spanish(PackOutput output) {
            super(output, "es_es");
        }

        @Override
        protected void addTranslations() {
            add("itemGroup.ultimatetransport", "Ultimate Transport System");

            add("block.ultimatetransport.energy_cable", "Cable de energía");
            add("block.ultimatetransport.fluid_cable", "Cable de fluidos");
            add("block.ultimatetransport.item_cable", "Cable de objetos");
            add("block.ultimatetransport.gas_cable", "Cable de gases");
            add("block.ultimatetransport.source_cable", "Tubería de maná");
            add("block.ultimatetransport.universal_cable", "Cable universal");
            add("block.ultimatetransport.tube", "Tubo de transporte");
            add("block.ultimatetransport.station", "Estación de tubo");
            add("block.ultimatetransport.station_horizontal", "Estación de tubo");
            add("item.ultimatetransport.tube_down", "Tubo de transporte (abajo)");
            add("item.ultimatetransport.tube_up", "Tubo de transporte (arriba)");
            add("item.ultimatetransport.tube_north", "Tubo de transporte (norte)");
            add("item.ultimatetransport.tube_south", "Tubo de transporte (sur)");
            add("item.ultimatetransport.tube_east", "Tubo de transporte (este)");
            add("item.ultimatetransport.tube_west", "Tubo de transporte (oeste)");
            add("tooltip.ultimatetransport.direction", "Dirección: %s");
            add("tooltip.ultimatetransport.direction.0", "ABAJO");
            add("tooltip.ultimatetransport.direction.1", "ARRIBA");
            add("tooltip.ultimatetransport.direction.2", "NORTE");
            add("tooltip.ultimatetransport.direction.3", "SUR");
            add("tooltip.ultimatetransport.direction.4", "ESTE");
            add("tooltip.ultimatetransport.direction.5", "OESTE");
            add("tooltip.ultimatetransport.autodirection.1", "Apunta hacia la cara donde lo colocas");
            add("tooltip.ultimatetransport.autodirection.2", "Colócalo en la cara norte de un bloque para un tubo al norte");
            add("tooltip.ultimatetransport.autodirection.3", "Fabrícalo para fijar una dirección");
            add("ultimatetransport.configuration.max_tube_speed", "Velocidad máxima del tubo");
            add("ultimatetransport.configuration.suppress_walk", "Silenciar los pasos al viajar");
            add("ultimatetransport.configuration.render_pass", "Capa de dibujado del tubo");
            add("ultimatetransport.configuration.lenient_culling", "Fundir tubos que se tocan");
            add("ultimatetransport.configuration.placement_preview", "Mostrar vista previa al colocar");

            add("item.ultimatetransport.basic_upgrade", "Mejora básica");
            add("item.ultimatetransport.improved_upgrade", "Mejora mejorada");
            add("item.ultimatetransport.advanced_upgrade", "Mejora avanzada");
            add("item.ultimatetransport.ultimate_upgrade", "Mejora definitiva");
            add("item.ultimatetransport.infinity_upgrade", "Mejora infinita");
            add("item.ultimatetransport.configurator", "Configurador");
            add("item.ultimatetransport.facade", "Fachada");
            add("item.ultimatetransport.facade.of", "Fachada de %s");
            add("tooltip.ultimatetransport.facade.blank", "Úsala sobre un bloque completo para copiarlo.");
            add("tooltip.ultimatetransport.facade.filled", "Úsala sobre una cara del cable para taparla. El configurador la quita.");
            String[] batteries = {"Básica", "Avanzada", "Reforzada", "Élite", "Industrial", "Suprema",
                    "Cuántica", "Estelar", "Cósmica", "Titánica", "Infinita"};
            for (int tier = 1; tier <= batteries.length; tier++) {
                add("block.ultimatetransport.energy_cell_" + tier, "Batería " + batteries[tier - 1]);
            }
            add("block.ultimatetransport.creative_energy_cell", "Batería Creativa");
            add("screen.ultimatetransport.cell_endless", "Sin fin");
            add("tooltip.ultimatetransport.cell_charge_slot", "Carga: la celda llena el objeto aquí.");
            add("tooltip.ultimatetransport.cell_discharge_slot", "Descarga: el objeto se vacía en la celda aquí.");

            add("ultimatetransport.cell_side.disabled", "Desactivado");
            add("ultimatetransport.cell_side.input", "Entrada");
            add("ultimatetransport.cell_side.output", "Salida");
            add("ultimatetransport.cell_side.both", "Entrada y salida");
            add("ultimatetransport.cell_display.none", "Nada");
            add("ultimatetransport.cell_display.bar", "Barra de carga");
            add("ultimatetransport.cell_display.io", "Entrada y salida");
            add("message.ultimatetransport.cell_display", "%s: %s");

            add("message.ultimatetransport.cell_charge", "%s / %s FE");
            add("tooltip.ultimatetransport.cell_charge", "%s / %s FE");
            add("tooltip.ultimatetransport.cell_transfer", "%s FE/t");

            add("ultimatetransport.transfer_type.energy", "Energía");
            add("ultimatetransport.transfer_type.fluid", "Fluidos");
            add("ultimatetransport.transfer_type.item", "Objetos");
            add("ultimatetransport.transfer_type.gas", "Gases");
            add("ultimatetransport.transfer_type.source", "Maná");
            add("ultimatetransport.transfer_type.universal", "Todo");

            add("ultimatetransport.connection_mode.none", "Desconectado");
            add("ultimatetransport.connection_mode.insert", "Insertar");
            add("ultimatetransport.connection_mode.extract", "Extraer");

            add("ultimatetransport.redstone_mode.ignored", "Ignora la redstone");
            add("ultimatetransport.redstone_mode.off_when_powered", "Apagado con señal");
            add("ultimatetransport.redstone_mode.on_when_powered", "Encendido con señal");
            add("ultimatetransport.redstone_mode.always_off", "Siempre apagado");

            add("ultimatetransport.distribution_mode.round_robin", "Reparto equitativo");
            add("ultimatetransport.distribution_mode.nearest", "Más cercano primero");
            add("ultimatetransport.distribution_mode.furthest", "Más lejano primero");
            add("ultimatetransport.distribution_mode.random", "Aleatorio");

            add("ultimatetransport.upgrade.none", "Sin mejora");
            add("ultimatetransport.upgrade.basic", "Básica");
            add("ultimatetransport.upgrade.improved", "Mejorada");
            add("ultimatetransport.upgrade.advanced", "Avanzada");
            add("ultimatetransport.upgrade.ultimate", "Definitiva");
            add("ultimatetransport.upgrade.infinity", "Infinita");

            add("ultimatetransport.direction.down", "Abajo");
            add("ultimatetransport.direction.up", "Arriba");
            add("ultimatetransport.direction.north", "Norte");
            add("ultimatetransport.direction.south", "Sur");
            add("ultimatetransport.direction.west", "Oeste");
            add("ultimatetransport.direction.east", "Este");

            add("message.ultimatetransport.side_mode", "%s: %s");

            add("screen.ultimatetransport.cable", "%s - %s");
            add("screen.ultimatetransport.filter", "Regla de filtro");
            add("screen.ultimatetransport.priority", "Prioridad: %s");
            add("screen.ultimatetransport.upgrade_slot", "Mejora");
            add("screen.ultimatetransport.filter.add", "Añadir");
            add("screen.ultimatetransport.filter.search", "Buscar");
            add("tooltip.ultimatetransport.theme", "Tema de la pantalla: %s");
            add("ultimatetransport.theme.dark", "Oscuro");
            add("ultimatetransport.theme.light", "Claro");
            add("tooltip.ultimatetransport.retrieve", "Sentido: %s");
            add("tooltip.ultimatetransport.retrieve.hint", "Recuperar tira de la red hacia este bloque.");
            add("ultimatetransport.retrieve.off", "Extraer");
            add("ultimatetransport.retrieve.on", "Recuperar");
            add("ultimatetransport.link.severed", "Desconectado");
            add("ultimatetransport.link.joined", "Conectado");
            add("screen.ultimatetransport.filter.edit", "Editar");
            add("screen.ultimatetransport.filter.remove", "Quitar");
            add("screen.ultimatetransport.filter.cancel", "Cancelar");
            add("screen.ultimatetransport.filter.submit", "Guardar");
            add("screen.ultimatetransport.filter.item_tag", "Objeto/Etiqueta");
            add("screen.ultimatetransport.filter.nbt", "Datos NBT");
            add("screen.ultimatetransport.filter.destination", "Destino");
            add("screen.ultimatetransport.filter.destination.any", "Cualquiera");
            add("screen.ultimatetransport.filter.any", "cualquier cosa");
            add("screen.ultimatetransport.cell_side", "%s: %s");
            add("ultimatetransport.filter_mode.whitelist", "Lista blanca");
            add("ultimatetransport.filter_mode.blacklist", "Lista negra");

            add("tooltip.ultimatetransport.unlimited", "Ilimitado");
            add("tooltip.ultimatetransport.upgrade.energy", "%s FE/t");
            add("tooltip.ultimatetransport.upgrade.fluid", "%s mB/t");
            add("tooltip.ultimatetransport.upgrade.item", "%s objetos cada %s ticks");
            add("tooltip.ultimatetransport.upgrade.item_tick", "%s objetos cada tick");
            add("tooltip.ultimatetransport.upgrade.gas", "%s unidades/t");
            add("tooltip.ultimatetransport.upgrade.redstone_yes", "Modo redstone");
            add("tooltip.ultimatetransport.upgrade.redstone_no", "Sin modo redstone");
            add("tooltip.ultimatetransport.upgrade.distribution_yes", "Modo de reparto");
            add("tooltip.ultimatetransport.upgrade.distribution_no", "Sin modo de reparto");
            add("tooltip.ultimatetransport.upgrade.filter_yes", "Reglas de filtro");
            add("tooltip.ultimatetransport.upgrade.filter_no", "Sin reglas de filtro");
            add("tooltip.ultimatetransport.upgrade.install", "Agachado y clic derecho en una cara del cable para colocarla");
            add("tooltip.ultimatetransport.upgrade.rules", "%s reglas de filtro (%s), guardadas en esta mejora");
            add("tooltip.ultimatetransport.configurator", "Clic en una cara para avanzarla, agachado para retroceder.");
            add("tooltip.ultimatetransport.configurator.sneak", "Agachado y rueda cambia de modo, o la tecla de modos abre la rueda.");
            add("tooltip.ultimatetransport.configurator.mode", "Modo: %s");
            add("message.ultimatetransport.configurator_mode", "Configurador: %s");
            add("ultimatetransport.configurator_mode.configurate_energy", "Configurar: Energía");
            add("ultimatetransport.configurator_mode.configurate_fluid", "Configurar: Fluido");
            add("ultimatetransport.configurator_mode.configurate_item", "Configurar: Objetos");
            add("ultimatetransport.configurator_mode.configurate_gas", "Configurar: Químicos");
            add("ultimatetransport.configurator_mode.configurate_source", "Configurar: Fuente");
            add("ultimatetransport.configurator_mode.empty", "Vaciar");
            add("ultimatetransport.configurator_mode.rotate", "Rotar");
            add("ultimatetransport.configurator_mode.wrench", "Llave");
            add("key.categories.ultimatetransport", "Ultimate Transport");
            add("key.ultimatetransport.configurator_menu", "Modos del configurador");
            add("message.ultimatetransport.configurator_unsupported", "Este bloque no tiene %s que configurar");
            add("ultimatetransport.face.top", "Arriba");
            add("ultimatetransport.face.bottom", "Abajo");
            add("ultimatetransport.face.front", "Frente");
            add("ultimatetransport.face.back", "Atrás");
            add("ultimatetransport.face.left", "Izquierda");
            add("ultimatetransport.face.right", "Derecha");
            add("screen.ultimatetransport.side_config.hint", "Clic izquierdo avanza, clic derecho retrocede.");
            add("screen.ultimatetransport.side_config.tab", "Configuración lateral");
            add("tooltip.ultimatetransport.cell_bank", "Banco de %s baterías");
            add("screen.ultimatetransport.side_config.eject", "Auto-expulsar");
            add("screen.ultimatetransport.side_config.eject_on", "Empujando a los vecinos");
            add("screen.ultimatetransport.side_config.eject_off", "Reteniendo todo");
            add("screen.ultimatetransport.side_config.close", "Cerrar");
            add("screen.ultimatetransport.side_config.clear", "Limpiar todas las caras");
            add("screen.ultimatetransport.side_config.unlinked", "Esta cara no tiene nada que configurar");
            add("config.ultimatetransport.gui_theme", "Tema de las pantallas");

            add("block.ultimatetransport.fuel_generator", "Generador de combustible");
            add("block.ultimatetransport.lava_generator", "Generador de lava");
            add("block.ultimatetransport.solar_generator", "Generador solar");
            add("block.ultimatetransport.nether_star_generator", "Generador de estrella del Nether");
            add("jei.ultimatetransport.fuel.rate", "%s FE/t");
            add("jei.ultimatetransport.fuel.duration", "%s");
            add("jei.ultimatetransport.fuel.ticks", "%s ticks");
            add("jei.ultimatetransport.generator.fuel_generator", "Quema todo lo que quemaría un horno, y durante exactamente lo mismo. Se le echa a mano, con una tolva o con un cable de objetos, y empuja su búfer a lo que tenga al lado.");
            add("jei.ultimatetransport.generator.lava_generator", "Bebe un cubo de lava cada vez. El cubo vuelve vacío a la ranura, así que un cable de objetos puede llevárselo y devolverlo lleno.");
            add("jei.ultimatetransport.generator.solar_generator", "Funciona de día, sin lluvia y con el bloque de encima viendo el cielo. No tiene ranura de combustible porque no come nada.");
            add("jei.ultimatetransport.generator.nether_star_generator", "Una estrella del Nether lo mantiene 20.000 ticks a 2.000 FE/t. El final de la escalera, y el único generador que alimenta él solo un tendido de cables ultimate.");
            add("screen.ultimatetransport.generator_running", "En marcha: %s FE/t");
            add("screen.ultimatetransport.generator_idle", "Parado");
            add("screen.ultimatetransport.generator_output", "Salida: %s FE/t");

            add("item.ultimatetransport.destination_tool", "Herramienta de destino");
            add("tooltip.ultimatetransport.destination_tool", "Clic derecho en la cara de un bloque para grabarla, luego suéltala en la ranura de destino de una regla.");
            add("message.ultimatetransport.destination", "%s, %s, %s (%s)");

            add("tooltip.ultimatetransport.redstone_mode", "Redstone: %s");
            add("tooltip.ultimatetransport.distribution", "Reparto: %s");
            add("tooltip.ultimatetransport.filter_mode", "Filtro: %s");
            add("tooltip.ultimatetransport.filter.pick", "Clic con un objeto para rellenar la regla. Agachado, la vacía.");
            add("tooltip.ultimatetransport.filter.item_tag", "Un id como minecraft:iron_ingot, o #c:ingots para una etiqueta.");
            add("tooltip.ultimatetransport.filter.nbt", "NBT opcional para afinar. Vacío acepta cualquiera.");
            add("tooltip.ultimatetransport.filter.nbt.exact", "El NBT debe coincidir exactamente");
            add("tooltip.ultimatetransport.filter.nbt.partial", "Basta con que el NBT lo contenga");
            add("tooltip.ultimatetransport.filter.inverted", "Invertida: acepta todo menos esto");
            add("tooltip.ultimatetransport.filter.not_inverted", "Acepta esto");
            add("tooltip.ultimatetransport.filter.destination", "Clic con la Herramienta de destino para enviar lo que cumpla esta regla a un solo sitio.");
            add("tooltip.ultimatetransport.filter.destination.clear", "Clic con la mano vacía para quitarlo.");

            add("config.jade.plugin_ultimatetransport.cable", "Cara del cable");
            add("config.jade.plugin_ultimatetransport.energy_cell", "Celda de energía");
            add("config.jade.plugin_ultimatetransport.tube", "Tubo de transporte");
            add("jade.ultimatetransport.tube_direction", "Lleva hacia: %s");
            add("jade.ultimatetransport.rules", "%s, %s reglas");
            add("jade.ultimatetransport.facade", "Fachada: %s");
            add("jade.ultimatetransport.travelling", "%s en tránsito");

            add("jei.ultimatetransport.cable", "Los cables mueven su carga al instante por la red. Configura cada cara con el configurador, o haz clic derecho con la mano vacía para modos, ranura de mejora y reglas de filtro.");
            add("jei.ultimatetransport.upgrade", "Va en una cara del cable. Sube el caudal de esa cara y es lo que le permite usar modos de redstone, modos de reparto y reglas de filtro.");
            add("jei.ultimatetransport.configurator", "Clic derecho en una cara del cable que toque una máquina para alternar entre insertar y extraer; el resto del tendido no tiene nada que configurar. En una celda cambia la cara entre entrada, salida, ambas y desactivada,. Agachado en el aire cambia el modo de la herramienta.");
            add("jei.ultimatetransport.facade", "Úsala sobre cualquier bloque completo para copiarlo, y luego sobre una cara del cable para taparla. La tapa es un panel de dos píxeles con un hueco para el brazo que la atraviese. El configurador la quita.");
            add("jei.ultimatetransport.destination_tool", "Clic derecho en la cara de un bloque para grabarla, y suelta la herramienta en la ranura de destino de una regla. Lo que cumpla esa regla irá solo ahí.");
            add("jei.ultimatetransport.player_tube", "Un tubo de cristal con dirección. Lo que entra acelera por el eje del tubo hasta el tope de velocidad, así que los tramos largos son rápidos y los cortos suaves. Dentro no hay daño por caída, así que un pozo vertical puede ser tan profundo como quieras.");
            add("jei.ultimatetransport.station", "Dos bloques de alto y hueca: por donde entras y sales. Estar sobre una estación con un tubo hacia arriba encima te sube a la red. Agáchate para bajarte en una estación intermedia en vez de llegar al final.");
            add("jei.ultimatetransport.energy_cell", "Guarda energía y la empuja a lo que tenga al lado, pero nunca a otra celda más llena, así un banco se iguala solo. Conserva la carga al romperla y funciona desde el inventario.");
        }
    }
}
