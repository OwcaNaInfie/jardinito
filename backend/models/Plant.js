const mongoose = require('mongoose');

const plantSchema = new mongoose.Schema({
    name: { type: String, required: true, unique: true },
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