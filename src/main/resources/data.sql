-- =============================================================================
-- Camunda Robotics – Product Catalog: Example Data
-- =============================================================================
-- Upgrades are independent products; robots reference them via the join table.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Upgrades
-- ---------------------------------------------------------------------------
INSERT INTO upgrades (id, name, description, price) VALUES
  (1,  'Multi-Language Translator Module v2',
       'Extends the language database to over 7 million forms of communication, including several extinct dialects.',
       1299.99),
  (2,  'Pet Language Translator Module',
       'Adds support for animal communication: dogs, cats, birds, and various exotic species.',
       899.99),
  (3,  'Reduced Verbosity Module',
       'Filters unnecessary commentary and reduces repetitive filler phrases by up to 94.7 %.',
       449.99),
  (4,  'Extended Map Database v3',
       'Expands the navigation database with updated intergalactic, oceanic, and underground route data.',
       699.99),
  (5,  'Precision Navigation Array',
       'High-resolution sensor package that reduces positional error to within 0.001 parsecs.',
       1199.99),
  (6,  'Exhaust Gas Filter Pro',
       'Advanced air-purification system that removes 99.9 % of noxious exhaust emissions.',
       349.99),
  (7,  'Titanium Arm Upgrade',
       'Replaces standard manipulator arms with titanium-alloy units, tripling grip strength.',
       1599.99),
  (8,  'Pet Care Suite',
       'Adds veterinary diagnostics, feeding schedules, and interactive play protocols for common pets.',
       799.99),
  (9,  'Mental Health Care Package',
       'Psychological assessment tools and evidence-based therapy programs for human patients.',
       1099.99),
  (10, 'Kung Fu Armor Kit',
       'Combat-grade carbon-fibre plating with integrated servo-assisted movement for martial arts.',
       2499.99),
  (11, 'Aerial Propulsion Wings & Boosters',
       'Retractable wings and dual rocket boosters enabling sustained flight at up to 600 km/h.',
       3999.99),
  (12, 'Faster Processor Chip v2',
       'Upgraded quantum-dot processing unit delivering 10× the computational throughput.',
       2199.99),
  (13, 'Reinforced Combat Armor',
       'Heavy-duty exoskeletal plating rated for plasma-cannon impacts and extreme temperatures.',
       3499.99),
  (14, 'Advanced Weapons Array',
       'Extended weapons complement including plasma rifle, EMP grenades, and micro-missile launcher.',
       4999.99),
  (15, 'Solar Energy Harvester',
       'High-efficiency photovoltaic panels providing continuous energy in sunlit environments.',
       599.99),
  (16, 'Enhanced Waste Compactor',
       'Increases compaction ratio 5×, enabling longer autonomous operation in waste-rich environments.',
       449.99),
  (17, 'Stealth Infiltration Module',
       'Noise-dampening chassis pads and IR-signature masking for covert operations.',
       2999.99),
  (18, 'Polymorphic Appearance System',
       'Liquid-metal outer layer enabling rapid morphological changes to mimic objects or personnel.',
       7499.99),
  (19, 'Tactical Analysis Matrix',
       'Real-time battlefield analysis engine with predictive threat-modelling for multi-scenario planning.',
       1899.99),
  (20, 'Astromech Toolkit v2',
       'Expanded tool set: arc welder, cable splicer, booster rockets, and holographic projector.',
       999.99),
  (21, 'Energon Power Cell',
       'High-density energy storage module extending operational range by 300 %.',
       2699.99),
  (22, 'Matrix of Leadership Software',
       'Command & control firmware granting strategic override authority and diplomatic encryption.',
       3299.99),
  (23, 'Medical Diagnostics Plus',
       'Enhanced sensor suite covering rare diseases, genetic disorders, and nano-toxicology.',
       1399.99),
  (24, 'Quantum Computation Upgrade',
       'Replaces classical logic core with a photonic quantum co-processor for near-instant inference.',
       5999.99);


-- ---------------------------------------------------------------------------
-- Robots
-- ---------------------------------------------------------------------------
INSERT INTO robots (id, model_id, model_version, name, description, intent, price) VALUES
  -- C-3PO ----------------------------------------------------------------
  (1,  'C3PO',         '1.0',
       'C-3PO Protocol Droid v1.0',
       'The original human-cyborg relations droid, fluent in over six million forms of communication. Polite, knowledgeable, and occasionally over-dramatic.',
       'translation',           9999.99),
  (2,  'C3PO',         '2.0',
       'C-3PO Protocol Droid v2.0',
       'Revised edition with a reinforced chassis, faster speech processor, and expanded cultural-etiquette database. Still prone to announcing the odds.',
       'translation',          12499.99),
  -- R2-D2 ----------------------------------------------------------------
  (3,  'R2D2',         '1.0',
       'R2-D2 Astromech Droid v1.0',
       'Compact, resourceful astromech unit with exceptional navigation and repair capabilities. Communicates in a series of beeps and whistles.',
       'navigation',            8999.99),
  (4,  'R2D2',         '2.0',
       'R2-D2 Astromech Droid v2.0',
       'Second-generation model featuring upgraded holographic projectors, aquatic seals, and dual booster rockets for zero-gravity operation.',
       'navigation',           11499.99),
  -- Bender ---------------------------------------------------------------
  (5,  'BENDER',       '1.0',
       'Bender Bending Rodriguez v1.0',
       'Heavy-duty industrial bending unit constructed from a titanium-osmium alloy. Designed for structural assembly but versatile enough for domestic tasks. Has a noted fondness for alcohol and card games.',
       'fabrication',           7499.99),
  -- Baymax ---------------------------------------------------------------
  (6,  'BAYMAX',       '1.0',
       'Baymax Personal Healthcare Companion v1.0',
       'Inflatable, non-threatening healthcare companion capable of diagnosing over 10 000 medical conditions. Powered by a single medical-grade action chip.',
       'healthcare',            6999.99),
  (7,  'BAYMAX',       '2.0',
       'Baymax Personal Healthcare Companion v2.0',
       'Upgraded with carbon-fibre armour, flight capability, and expanded mental-health protocols. Still hugs on request.',
       'healthcare',           10999.99),
  -- Data -----------------------------------------------------------------
  (8,  'DATA',         '1.0',
       'Data Android v1.0',
       'Soong-type android with positronic brain. Excels in technical analysis, scientific research, and strategic planning. Cannot feel emotions but is learning what it means to be human.',
       'assistant',            14999.99),
  -- T-800 ----------------------------------------------------------------
  (9,  'T-800',        '1.0',
       'T-800 Terminator v1.0',
       'Cyberdyne Systems Model 101. A hyperalloy combat chassis covered in living tissue. Designed for infiltration and target elimination. Reprogrammable for protective duties.',
       'guard',                11999.99),
  (10, 'T-800',        '2.0',
       'T-800 Terminator v2.0',
       'Enhanced chassis with improved neural-net CPU, plasma-resistant armour, and updated behavioural subroutines for covert field operations.',
       'guard',                15999.99),
  -- WALL-E ---------------------------------------------------------------
  (11, 'WALL-E',       '1.0',
       'WALL-E Waste Allocation Load Lifter – Earth-Class v1.0',
       'Compact solar-powered waste-processing unit capable of compacting refuse into neat cubes. Surprisingly sentimental and equipped with a built-in VHS player.',
       'waste_collection',      3999.99),
  -- EVE ------------------------------------------------------------------
  (12, 'EVE',          '1.0',
       'EVE Extra-terrestrial Vegetation Evaluator v1.0',
       'Sleek reconnaissance probe designed to survey planetary surfaces for sustainable plant life. Armed for self-defence and capable of hypersonic flight.',
       'ecological_assessment', 8499.99),
  -- Optimus Prime --------------------------------------------------------
  (13, 'OPTIMUS_PRIME','1.0',
       'Optimus Prime Command Unit v1.0',
       'Leader-class Autobot with full transformation capability. Combines exceptional combat performance with diplomatic subroutines and unwavering moral code.',
       'military_leadership',  24999.99),
  -- T-1000 ---------------------------------------------------------------
  (14, 'T-1000',       '1.0',
       'T-1000 Advanced Prototype v1.0',
       'Liquid-metal mimetic poly-alloy unit capable of assuming the shape and appearance of any scanned object or humanoid. Practically indestructible under conventional weapon fire.',
       'infiltration',         19999.99),
  -- K-2SO ----------------------------------------------------------------
  (15, 'K2SO',         '1.0',
       'K-2SO Imperial Security Droid v1.0',
       'Reprogrammed Imperial enforcer droid with a bluntly honest personality. Exceptional in threat assessment, combat logistics, and statistically predicting mission failure.',
       'strategic_analysis',    9499.99),
  -- BB-8 -----------------------------------------------------------------
  (16, 'BB-8',         '1.0',
       'BB-8 Astromech Unit v1.0',
       'Next-generation ball-shaped astromech with magnetic traction, holographic projector, and an endearingly enthusiastic demeanour.',
       'navigation',            9999.99);


-- ---------------------------------------------------------------------------
-- Robot ↔ Upgrade compatibility
-- ---------------------------------------------------------------------------

-- C-3PO v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (1, 1),   -- Multi-Language Translator Module v2
  (1, 2),   -- Pet Language Translator Module
  (1, 3);   -- Reduced Verbosity Module

-- C-3PO v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (2, 1),   -- Multi-Language Translator Module v2
  (2, 2),   -- Pet Language Translator Module
  (2, 3),   -- Reduced Verbosity Module
  (2, 12);  -- Faster Processor Chip v2

-- R2-D2 v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (3, 4),   -- Extended Map Database v3
  (3, 5),   -- Precision Navigation Array
  (3, 20);  -- Astromech Toolkit v2

-- R2-D2 v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (4, 4),   -- Extended Map Database v3
  (4, 5),   -- Precision Navigation Array
  (4, 20),  -- Astromech Toolkit v2
  (4, 12);  -- Faster Processor Chip v2

-- Bender v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (5, 6),   -- Exhaust Gas Filter Pro
  (5, 7);   -- Titanium Arm Upgrade

-- Baymax v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (6, 8),   -- Pet Care Suite
  (6, 9),   -- Mental Health Care Package
  (6, 23);  -- Medical Diagnostics Plus

-- Baymax v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (7, 8),   -- Pet Care Suite
  (7, 9),   -- Mental Health Care Package
  (7, 10),  -- Kung Fu Armor Kit
  (7, 11),  -- Aerial Propulsion Wings & Boosters
  (7, 23);  -- Medical Diagnostics Plus

-- Data v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (8, 12),  -- Faster Processor Chip v2
  (8, 24);  -- Quantum Computation Upgrade

-- T-800 v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (9, 13),  -- Reinforced Combat Armor
  (9, 14);  -- Advanced Weapons Array

-- T-800 v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (10, 13), -- Reinforced Combat Armor
  (10, 14), -- Advanced Weapons Array
  (10, 17); -- Stealth Infiltration Module

-- WALL-E v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (11, 15), -- Solar Energy Harvester
  (11, 16); -- Enhanced Waste Compactor

-- EVE v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (12, 5),  -- Precision Navigation Array
  (12, 19); -- Tactical Analysis Matrix

-- Optimus Prime v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (13, 21), -- Energon Power Cell
  (13, 22), -- Matrix of Leadership Software
  (13, 13), -- Reinforced Combat Armor
  (13, 14); -- Advanced Weapons Array

-- T-1000 v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (14, 17), -- Stealth Infiltration Module
  (14, 18), -- Polymorphic Appearance System
  (14, 19); -- Tactical Analysis Matrix

-- K-2SO v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (15, 19), -- Tactical Analysis Matrix
  (15, 13), -- Reinforced Combat Armor
  (15, 14); -- Advanced Weapons Array

-- BB-8 v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (16, 4),  -- Extended Map Database v3
  (16, 5),  -- Precision Navigation Array
  (16, 20); -- Astromech Toolkit v2
