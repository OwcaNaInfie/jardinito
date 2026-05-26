const mongoose = require('mongoose');

const PlantColor = ['RED', 'PINK', 'YELLOW', 'WHITE', 'PURPLE', 'ORANGE', 'BLUE'];
const PlantSize = ['small', 'medium', 'large'];

const plantSchema = new mongoose.Schema({
    name: { type: String, required: true, unique: true },
    nameKey: { type: String, required: true, unique: true },
    descriptionKey: { type: String, required: true },
    colors: {
        type: [{ type: String, enum: PlantColor }],
        required: true,
        validate: v => v.length >= 1
    },
    size: { type: String, enum: PlantSize, required: true },
    images: {
        small: { type: String, required: true },
        medium: { type: String, required: true },
        mediumOutlined: { type: String, required: true },
        large: { type: String, required: true }
    },
    witheredImages: {
        small: { type: String, required: true },
        medium: { type: String, required: true },
        mediumOutlined: { type: String, required: true }
    },
    minDurationDev: { type: Number, required: true },
    minDuration: { type: Number, required: true },
    price: { type: Number, required: true, default: 0 }
});

module.exports = mongoose.model('Plant', plantSchema);