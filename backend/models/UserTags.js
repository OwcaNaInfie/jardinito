const mongoose = require('mongoose');

const tagSchema = new mongoose.Schema({
    name: { type: String, required: true, maxlength: 30 },
    color: { type: String, required: true }
}, { timestamps: true });

const userTagsSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        unique: true
    },
    tags: [tagSchema]
}, { timestamps: true });

module.exports = mongoose.model('UserTags', userTagsSchema);