const Plant = require('../models/Plant');

exports.findAll = () => Plant.find();
exports.findById = (plantId) => Plant.findById(plantId);
exports.findFree = () => Plant.find({ price: 0 }).select('_id');