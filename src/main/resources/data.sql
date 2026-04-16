-- =============================================================================
-- Camunda Robotics – Product Catalog: Example Data
-- =============================================================================
-- Upgrades are independent products; robots reference them via the join table.
-- Intent values must match the RobotIntent enum names (stored as strings).
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
       'Upgraded quantum-dot processing unit delivering 10x the computational throughput.',
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
       'Increases compaction ratio 5x, enabling longer autonomous operation in waste-rich environments.',
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
  -- C-3PO / C-3PX protocol droid line ------------------------------------
  (1,  'C3PO',         '1.0',
       'C-3PO Protocol Droid v1.0',
       'The original human-cyborg relations droid, fluent in over six million forms of communication. Polite, knowledgeable, and occasionally over-dramatic.',
       'TRANSLATION',           9999.99),
  -- C-3PX is the next-generation protocol droid model that succeeded C-3PO
  (2,  'C3PX',         '1.0',
       'C-3PX Protocol Droid v1.0',
       'Successor to the C-3PO line: reinforced durasteel chassis, dual-core speech processor, and an expanded cultural-etiquette database covering 8.4 million communication forms.',
       'TRANSLATION',          12499.99),
  -- R-series astromech line: R2 -> R3 -> R4 -> R5 -----------------------
  (3,  'ASTROMECH',    'R2',
       'R2-D2 Astromech Droid',
       'Compact, resourceful astromech unit with exceptional navigation and repair capabilities. Communicates in a series of beeps and whistles.',
       'NAVIGATION',            8999.99),
  (4,  'ASTROMECH',    'R3',
       'R3-T7 Astromech Droid',
       'Third-generation astromech with a transparent dome head for enhanced sensor visibility and an extended memory core for storing complex star-charts.',
       'NAVIGATION',           10499.99),
  (5,  'ASTROMECH',    'R4',
       'R4-P17 Astromech Droid',
       'Fourth-generation unit optimised for starfighter co-pilot duties, featuring a low-profile conical dome and improved hyperspace jump calculation routines.',
       'NAVIGATION',           11299.99),
  (6,  'ASTROMECH',    'R5',
       'R5-D4 Astromech Droid',
       'Fifth-generation model with an updated motivator and wider tool-bay. Budget-friendly option for moisture farmers and independent pilots.',
       'NAVIGATION',            7999.99),
  -- BB-series: next evolution of the astromech line ---------------------
  (7,  'ASTROMECH',    'BB',
       'BB-8 Astromech Unit',
       'Ball-shaped next-generation astromech with magnetic traction, holographic projector, and an endearingly enthusiastic demeanour. Engineered for extreme-terrain operations.',
       'NAVIGATION',            9999.99),
  -- Bender ---------------------------------------------------------------
  (8,  'BENDER',       '1.0',
       'Bender Bending Rodriguez v1.0',
       'Heavy-duty industrial bending unit constructed from a titanium-osmium alloy. Designed for structural assembly but versatile enough for domestic tasks. Has a noted fondness for alcohol and card games.',
       'FABRICATION',           7499.99),
  -- Baymax ---------------------------------------------------------------
  (9,  'BAYMAX',       '1.0',
       'Baymax Personal Healthcare Companion v1.0',
       'Inflatable, non-threatening healthcare companion capable of diagnosing over 10 000 medical conditions. Powered by a single medical-grade action chip.',
       'HEALTHCARE',            6999.99),
  (10, 'BAYMAX',       '2.0',
       'Baymax Personal Healthcare Companion v2.0',
       'Upgraded with carbon-fibre armour, flight capability, and expanded mental-health protocols. Still hugs on request.',
       'HEALTHCARE',           10999.99),
  -- Data -----------------------------------------------------------------
  (11, 'DATA',         '1.0',
       'Data Android v1.0',
       'Soong-type android with positronic brain. Excels in technical analysis, scientific research, and strategic planning. Cannot feel emotions but is learning what it means to be human.',
       'ASSISTANT',            14999.99),
  -- T-800 / T-1000 terminator line: T-1000 is the successor of T-800 ---
  (12, 'T-800',        '1.0',
       'T-800 Terminator v1.0',
       'Cyberdyne Systems Model 101. A hyperalloy combat chassis covered in living tissue. Designed for infiltration and target elimination. Reprogrammable for protective duties.',
       'GUARD',                11999.99),
  (13, 'T-800',        '2.0',
       'T-800 Terminator v2.0',
       'Enhanced chassis with improved neural-net CPU, plasma-resistant armour, and updated behavioural subroutines for covert field operations.',
       'GUARD',                15999.99),
  -- T-1000 is the next-generation successor model to the T-800 line ----
  (14, 'T-800',        'T-1000',
       'T-1000 Advanced Prototype',
       'Liquid-metal mimetic poly-alloy unit capable of assuming the shape and appearance of any scanned object or humanoid. Practically indestructible under conventional weapon fire.',
       'GUARD',                19999.99),
  -- WALL-E ---------------------------------------------------------------
  (15, 'WALL-E',       '1.0',
       'WALL-E Waste Allocation Load Lifter – Earth-Class v1.0',
       'Compact solar-powered waste-processing unit capable of compacting refuse into neat cubes. Surprisingly sentimental and equipped with a built-in VHS player.',
       'FABRICATION',           3999.99),
  -- EVE ------------------------------------------------------------------
  (16, 'EVE',          '1.0',
       'EVE Extra-terrestrial Vegetation Evaluator v1.0',
       'Sleek reconnaissance probe designed to survey planetary surfaces for sustainable plant life. Armed for self-defence and capable of hypersonic flight.',
       'ASSISTANT',             8499.99),
  -- Optimus Prime --------------------------------------------------------
  (17, 'OPTIMUS_PRIME','1.0',
       'Optimus Prime Command Unit v1.0',
       'Leader-class Autobot with full transformation capability. Combines exceptional combat performance with diplomatic subroutines and unwavering moral code.',
       'GUARD',                24999.99),
  -- K-2SO ----------------------------------------------------------------
  (18, 'K2SO',         '1.0',
       'K-2SO Imperial Security Droid v1.0',
       'Reprogrammed Imperial enforcer droid with a bluntly honest personality. Exceptional in threat assessment, combat logistics, and statistically predicting mission failure.',
       'ASSISTANT',             9499.99);


-- ---------------------------------------------------------------------------
-- Robot <-> Upgrade compatibility
-- ---------------------------------------------------------------------------

-- C-3PO v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (1, 1),   -- Multi-Language Translator Module v2
  (1, 2),   -- Pet Language Translator Module
  (1, 3);   -- Reduced Verbosity Module

-- C-3PX v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (2, 1),   -- Multi-Language Translator Module v2
  (2, 2),   -- Pet Language Translator Module
  (2, 3),   -- Reduced Verbosity Module
  (2, 12);  -- Faster Processor Chip v2

-- R2-D2 (ASTROMECH R2)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (3, 4),   -- Extended Map Database v3
  (3, 5),   -- Precision Navigation Array
  (3, 20);  -- Astromech Toolkit v2

-- R3-T7 (ASTROMECH R3)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (4, 4),   -- Extended Map Database v3
  (4, 5),   -- Precision Navigation Array
  (4, 20),  -- Astromech Toolkit v2
  (4, 12);  -- Faster Processor Chip v2

-- R4-P17 (ASTROMECH R4)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (5, 4),   -- Extended Map Database v3
  (5, 5),   -- Precision Navigation Array
  (5, 20);  -- Astromech Toolkit v2

-- R5-D4 (ASTROMECH R5)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (6, 4),   -- Extended Map Database v3
  (6, 20);  -- Astromech Toolkit v2

-- BB-8 (ASTROMECH BB)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (7, 4),   -- Extended Map Database v3
  (7, 5),   -- Precision Navigation Array
  (7, 20);  -- Astromech Toolkit v2

-- Bender v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (8, 6),   -- Exhaust Gas Filter Pro
  (8, 7);   -- Titanium Arm Upgrade

-- Baymax v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (9, 8),   -- Pet Care Suite
  (9, 9),   -- Mental Health Care Package
  (9, 23);  -- Medical Diagnostics Plus

-- Baymax v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (10, 8),  -- Pet Care Suite
  (10, 9),  -- Mental Health Care Package
  (10, 10), -- Kung Fu Armor Kit
  (10, 11), -- Aerial Propulsion Wings & Boosters
  (10, 23); -- Medical Diagnostics Plus

-- Data v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (11, 12), -- Faster Processor Chip v2
  (11, 24); -- Quantum Computation Upgrade

-- T-800 v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (12, 13), -- Reinforced Combat Armor
  (12, 14); -- Advanced Weapons Array

-- T-800 v2.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (13, 13), -- Reinforced Combat Armor
  (13, 14), -- Advanced Weapons Array
  (13, 17); -- Stealth Infiltration Module

-- T-1000 (T-800 T-1000)
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (14, 17), -- Stealth Infiltration Module
  (14, 18), -- Polymorphic Appearance System
  (14, 19); -- Tactical Analysis Matrix

-- WALL-E v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (15, 15), -- Solar Energy Harvester
  (15, 16); -- Enhanced Waste Compactor

-- EVE v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (16, 5),  -- Precision Navigation Array
  (16, 19); -- Tactical Analysis Matrix

-- Optimus Prime v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (17, 21), -- Energon Power Cell
  (17, 22), -- Matrix of Leadership Software
  (17, 13), -- Reinforced Combat Armor
  (17, 14); -- Advanced Weapons Array

-- K-2SO v1.0
INSERT INTO robot_compatible_upgrades (robot_id, upgrade_id) VALUES
  (18, 19), -- Tactical Analysis Matrix
  (18, 13), -- Reinforced Combat Armor
  (18, 14); -- Advanced Weapons Array


-- =============================================================================
-- Camunda Robotics – Customer Database: Example Data
-- =============================================================================
-- Customers:
--   1 – Zee          (Berlin, Germany)          robot: WALL-E
--   2 – Luke         (Tatooine, Outer Rim)      robots: C3PO, R2-D2
--   3 – Philip J.    (New York, USA)            robot: Bender
--   4 – Hiro         (San Francisco, USA)       robot: Baymax
--   5 – Jean-Luc     (Enterprise, Deep Space)   robot: Data
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Customers
-- ---------------------------------------------------------------------------
INSERT INTO customers (id, name, email, address_street, address_city, address_country,
                        payment_method, payment_reference) VALUES
  (1, 'Zee',       'zee@camunda-robotics.io',
   'Unter den Linden 1', 'Berlin', 'Germany',
   'CREDIT_CARD', '**** **** **** 4200'),

  (2, 'Luke',      'luke.skywalker@tatooine.galaxy',
   'Moisture Farm, Jundland Wastes', 'Tatooine', 'Outer Rim Territories',
   'GALACTIC_CREDITS', 'GC-77890-SKY'),

  (3, 'Philip J.', 'p.fry@planet-express.com',
   '57th Street', 'New York', 'USA',
   'CREDIT_CARD', '**** **** **** 3000'),

  (4, 'Hiro',      'hiro.hamada@sfit.edu',
   '1234 Lucky Cat Cafe, Akihabara District', 'San Francisco', 'USA',
   'PAYPAL', 'hiro.hamada@sfit.edu'),

  (5, 'Jean-Luc',  'picard@starfleet.fed',
   'Ready Room, Deck 8', 'USS Enterprise NCC-1701-D', 'United Federation of Planets',
   'FEDERATION_CREDITS', 'FC-1701D-CAP');

-- ---------------------------------------------------------------------------
-- Orders
-- ---------------------------------------------------------------------------

-- Zee's orders (robot: WALL-E)
INSERT INTO orders (id, customer_id, order_date,
                    shipment_address_street, shipment_address_city, shipment_address_country,
                    shipment_date, payment_date, payment_amount) VALUES
  (1, 1, '2025-01-10',
   'Zossener Str. 55-58', 'Berlin', 'Germany',
   '2025-01-15', '2025-01-10', 3999.99),
  (2, 1, '2025-06-01',
   'Zossener Str. 55-58', 'Berlin', 'Germany',
   '2025-06-07', '2025-06-01', 449.99);

-- Luke's orders (robots: C3PO, R2-D2)
INSERT INTO orders (id, customer_id, order_date,
                    shipment_address_street, shipment_address_city, shipment_address_country,
                    shipment_date, payment_date, payment_amount) VALUES
  (3, 2, '2024-11-20',
   'Moisture Farm, Jundland Wastes', 'Tatooine', 'Outer Rim Territories',
   '2024-11-30', '2024-11-20', 9999.99),
  (4, 2, '2025-02-14',
   'Moisture Farm, Jundland Wastes', 'Tatooine', 'Outer Rim Territories',
   '2025-02-21', '2025-02-14', 8999.99);

-- Philip J.'s orders (robot: Bender)
INSERT INTO orders (id, customer_id, order_date,
                    shipment_address_street, shipment_address_city, shipment_address_country,
                    shipment_date, payment_date, payment_amount) VALUES
  (5, 3, '2025-03-01',
   '57th Street', 'New York', 'USA',
   '2025-03-08', '2025-03-01', 7499.99);

-- Hiro's orders (robot: Baymax)
INSERT INTO orders (id, customer_id, order_date,
                    shipment_address_street, shipment_address_city, shipment_address_country,
                    shipment_date, payment_date, payment_amount) VALUES
  (6, 4, '2024-12-24',
   '1234 Lucky Cat Cafe, Akihabara District', 'San Francisco', 'USA',
   '2024-12-29', '2024-12-24', 6999.99),
  (7, 4, '2025-04-05',
   '1234 Lucky Cat Cafe, Akihabara District', 'San Francisco', 'USA',
   '2025-04-12', '2025-04-05', 799.99);

-- Jean-Luc's orders (robot: Data)
INSERT INTO orders (id, customer_id, order_date,
                    shipment_address_street, shipment_address_city, shipment_address_country,
                    shipment_date, payment_date, payment_amount) VALUES
  (8, 5, '2025-05-10',
   'Ready Room, Deck 8', 'USS Enterprise NCC-1701-D', 'United Federation of Planets',
   '2025-05-20', '2025-05-10', 14999.99),
  (9, 5, '2025-09-01',
   'Ready Room, Deck 8', 'USS Enterprise NCC-1701-D', 'United Federation of Planets',
   '2025-09-08', '2025-09-01', 5999.99);

-- ---------------------------------------------------------------------------
-- Order Items
-- robot_id references robots(id); upgrade_id references upgrades(id)
-- Exactly one of robot_id or upgrade_id is set per row.
-- ---------------------------------------------------------------------------

-- Order 1: Zee buys WALL-E (robots.id = 15)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (1, 15, 1);

-- Order 2: Zee buys Enhanced Waste Compactor upgrade (upgrades.id = 16)
INSERT INTO order_items (order_id, upgrade_id, quantity) VALUES
  (2, 16, 1);

-- Order 3: Luke buys C-3PO (robots.id = 1)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (3, 1, 1);

-- Order 4: Luke buys R2-D2 (robots.id = 3)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (4, 3, 1);

-- Order 5: Philip J. buys Bender (robots.id = 8)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (5, 8, 1);

-- Order 6: Hiro buys Baymax v1.0 (robots.id = 9)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (6, 9, 1);

-- Order 7: Hiro buys Pet Care Suite for Baymax (upgrades.id = 8)
INSERT INTO order_items (order_id, upgrade_id, quantity) VALUES
  (7, 8, 1);

-- Order 8: Jean-Luc buys Data android (robots.id = 11)
INSERT INTO order_items (order_id, robot_id, quantity) VALUES
  (8, 11, 1);

-- Order 9: Jean-Luc buys Quantum Computation Upgrade for Data (upgrades.id = 24)
INSERT INTO order_items (order_id, upgrade_id, quantity) VALUES
  (9, 24, 1);
