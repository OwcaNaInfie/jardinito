const mongoose = require('mongoose');

const tagSnapshotSchema = new mongoose.Schema({
    tagId: { type: mongoose.Schema.Types.ObjectId, required: true },
    name: { type: String, required: true },
    color: { type: String, required: true }
}, { _id: false });

const sessionSchema = new mongoose.Schema({
    userId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    plantId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Plant',
        required: true
    },
    tag: tagSnapshotSchema,
    plannedDuration: { type: Number, required: true },
    actualDuration: { type: Number, default: null },
    status: {
        type: String,
        enum: ['completed', 'failed'],
        required: true
    },
    coinsEarned: { type: Number, default: 0 },
    startedAt: { type: Date, required: true },
    completedAt: { type: Date, default: null }
}, { timestamps: true });

// Indeksy dla filtrowania po dacie
sessionSchema.index({ userId: 1, startedAt: -1 });
sessionSchema.index({ userId: 1, status: 1 });
sessionSchema.index({ userId: 1, startedAt: -1, status: 1 });

module.exports = mongoose.model('Session', sessionSchema);