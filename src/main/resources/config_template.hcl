// All the Items you don't want to be replaced by pick block
// Single item id's and tags can be specified. Tags need to be prepended with a #
[dont_replace]
#minecraft:boats
// Custom tag that contains all tools
#betterpickblock:tools
// Custom tag that contains all buckets with no fish in them
#betterpickblock:buckets
minecraft:flint_and_steel
minecraft:shears
// Alternatives for blocks. For example: if you don't have stone in your inventory and pick stone, cobblestone will be moved to your hand instead if you have it.
// Synax [block you can pick]->[alternative]
// Other [block you can pick]->[first alternative]|[second alternative]|[third]
// This can be done with unlimited alternatives
[block_alternatives]
minecraft:stone->minecraft:cobblestone
minecraft:farmland->minecraft:dirt|minecraft:grass_block
minecraft:grass_block->minecraft:dirt