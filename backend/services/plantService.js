const plantRepository = require('../repositories/plantRepository');

exports.getAllPlants = () => plantRepository.findAll();
exports.getPlantById = (plantId) => plantRepository.findById(plantId);