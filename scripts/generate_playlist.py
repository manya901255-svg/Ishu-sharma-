import wave
import math
import struct
import subprocess
import os

SAMPLE_RATE = 22050

def make_wav(filename, samples, sample_rate=SAMPLE_RATE):
    with wave.open(filename, 'w') as wav_file:
        wav_file.setparams((1, 2, sample_rate, len(samples), "NONE", "not compressed"))
        packed = bytearray()
        for s in samples:
            val = int(max(-32767, min(32767, s * 32767.0)))
            packed.extend(struct.pack('<h', val))
        wav_file.writeframes(packed)

def convert_to_mp3(wav_path, mp3_path):
    cmd = ["ffmpeg", "-y", "-i", wav_path, "-codec:a", "libmp3lame", "-b:a", "96k", mp3_path]
    subprocess.run(cmd, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if os.path.exists(wav_path):
        os.remove(wav_path)
    print(f"Created: {mp3_path} ({os.path.getsize(mp3_path)} bytes)")

# Frequency map
NOTES = {
    'C3': 130.81, 'D3': 146.83, 'E3': 164.81, 'F3': 174.61, 'G3': 196.00, 'A3': 220.00, 'B3': 246.94,
    'C4': 261.63, 'D4': 293.66, 'E4': 329.63, 'F4': 349.23, 'F#4': 369.99, 'G4': 392.00, 'G#4': 415.30, 'A4': 440.00, 'B4': 493.88,
    'C5': 523.25, 'D5': 587.33, 'E5': 659.25, 'F5': 698.46, 'F#5': 739.99, 'G5': 783.99, 'A5': 880.00, 'B5': 987.77,
    'C6': 1046.50, 'D6': 1174.66, 'E6': 1318.51, 'G6': 1567.98
}

def add_sitar_note(samples, start_t, dur, freq, amp=0.22):
    s_idx = int(start_t * SAMPLE_RATE)
    n_samples = int(dur * SAMPLE_RATE)
    for i in range(n_samples):
        idx = s_idx + i
        if idx >= len(samples): break
        t = i / SAMPLE_RATE
        attack = min(1.0, t / 0.008)
        decay = math.exp(-3.2 * t / dur)
        # Sitar rich metallic buzz / jawari harmonics
        val = (
            0.60 * math.sin(2.0 * math.pi * freq * t) +
            0.28 * math.sin(4.0 * math.pi * freq * t) +
            0.18 * math.sin(6.0 * math.pi * freq * t) +
            0.10 * math.sin(8.0 * math.pi * freq * t) +
            0.08 * math.sin(10.0 * math.pi * freq * t)
        )
        samples[idx] += amp * val * decay * attack

def add_flute_note(samples, start_t, dur, freq, amp=0.20):
    s_idx = int(start_t * SAMPLE_RATE)
    n_samples = int(dur * SAMPLE_RATE)
    for i in range(n_samples):
        idx = s_idx + i
        if idx >= len(samples): break
        t = i / SAMPLE_RATE
        # Flute breath envelope
        attack = min(1.0, t / 0.04)
        release = min(1.0, (dur - t) / 0.04) if t > dur - 0.04 else 1.0
        vibrato = 1.0 + 0.006 * math.sin(2.0 * math.pi * 5.5 * t) if t > 0.15 else 1.0
        val = (
            0.82 * math.sin(2.0 * math.pi * freq * vibrato * t) +
            0.15 * math.sin(4.0 * math.pi * freq * t) +
            0.06 * math.sin(6.0 * math.pi * freq * t)
        )
        samples[idx] += amp * val * attack * release

def add_piano_note(samples, start_t, dur, freq, amp=0.22):
    s_idx = int(start_t * SAMPLE_RATE)
    n_samples = int(dur * SAMPLE_RATE)
    for i in range(n_samples):
        idx = s_idx + i
        if idx >= len(samples): break
        t = i / SAMPLE_RATE
        attack = min(1.0, t / 0.005)
        decay = math.exp(-2.5 * t / dur)
        val = (
            0.75 * math.sin(2.0 * math.pi * freq * t) +
            0.22 * math.sin(4.0 * math.pi * freq * t) +
            0.08 * math.sin(6.0 * math.pi * freq * t)
        )
        samples[idx] += amp * val * decay * attack

def add_tabla_stroke(samples, start_t, is_bayan=True, amp=0.25):
    s_idx = int(start_t * SAMPLE_RATE)
    dur = 0.28 if is_bayan else 0.14
    n_samples = int(dur * SAMPLE_RATE)
    base_freq = 95.0 if is_bayan else 320.0
    for i in range(n_samples):
        idx = s_idx + i
        if idx >= len(samples): break
        t = i / SAMPLE_RATE
        decay = math.exp(-12.0 * t) if is_bayan else math.exp(-22.0 * t)
        # Bayan pitch glide downward
        freq = base_freq * (1.0 + 0.4 * math.exp(-20.0 * t))
        val = math.sin(2.0 * math.pi * freq * t)
        samples[idx] += amp * val * decay

def add_tanpura_drone(samples, dur, root_freq=130.81, amp=0.08):
    # Sa - Pa - Sa strings
    strings = [root_freq * 1.5, root_freq * 2.0, root_freq * 2.0, root_freq]
    for cycle in range(int(dur / 4.0) + 1):
        for s_num, f in enumerate(strings):
            t_on = cycle * 4.0 + s_num * 0.9
            if t_on < dur:
                add_sitar_note(samples, t_on, 3.5, f, amp=amp)

# --- Track 3: Royal Court Sitar & Flute Raga ---
def generate_track_3():
    dur = 28.0
    samples = [0.0] * int(dur * SAMPLE_RATE)
    add_tanpura_drone(samples, dur, root_freq=NOTES['C3'], amp=0.07)
    
    # Sitar melody (Raag Yaman / Kalyan feel)
    sitar_melody = [
        (0.5, 0.8, 'E4'), (1.4, 0.8, 'G4'), (2.3, 1.2, 'A4'), (3.6, 1.5, 'B4'),
        (5.2, 1.0, 'A4'), (6.3, 1.2, 'G4'), (7.6, 1.8, 'E4'), (9.5, 2.0, 'D4'),
        (11.6, 0.9, 'G4'), (12.6, 0.9, 'B4'), (13.6, 1.4, 'D5'), (15.1, 1.6, 'C5'),
        (16.8, 1.0, 'B4'), (17.9, 1.2, 'A4'), (19.2, 1.8, 'G4'), (21.1, 2.2, 'C4'),
        (23.4, 1.2, 'E4'), (24.7, 1.5, 'G4'), (26.3, 1.6, 'C5')
    ]
    for st, d, note in sitar_melody:
        add_sitar_note(samples, st, d, NOTES[note], amp=0.22)
    
    # Flute counter-melody
    flute_melody = [
        (4.0, 2.0, 'E5'), (6.5, 2.5, 'D5'), (9.5, 2.2, 'G4'),
        (15.5, 2.2, 'E5'), (18.0, 2.5, 'D5'), (21.5, 3.0, 'C5')
    ]
    for st, d, note in flute_melody:
        add_flute_note(samples, st, d, NOTES[note], amp=0.18)
        
    # Soft tabla beats on 4/4 meter
    for t in range(0, int(dur), 1):
        add_tabla_stroke(samples, t + 0.0, is_bayan=True, amp=0.16)
        add_tabla_stroke(samples, t + 0.5, is_bayan=False, amp=0.12)
        
    wav_path = "/tmp/track3.wav"
    mp3_path = "app/src/main/res/raw/royal_court_raga.mp3"
    make_wav(wav_path, samples)
    convert_to_mp3(wav_path, mp3_path)

# --- Track 4: Romantic Piano & Santoor Serenade ---
def generate_track_4():
    dur = 26.0
    samples = [0.0] * int(dur * SAMPLE_RATE)
    
    # Piano chords & arpeggios
    chords = [
        (0.0, 3.0, ['C4', 'E4', 'G4', 'C5']),
        (3.0, 3.0, ['A3', 'C4', 'E4', 'A4']),
        (6.0, 3.0, ['F3', 'A3', 'C4', 'F4']),
        (9.0, 3.0, ['G3', 'B3', 'D4', 'G4']),
        (12.0, 3.0, ['C4', 'E4', 'G4', 'C5']),
        (15.0, 3.0, ['A3', 'C4', 'E4', 'A4']),
        (18.0, 3.0, ['F3', 'A3', 'C4', 'F4']),
        (21.0, 4.0, ['G3', 'B3', 'D4', 'C5'])
    ]
    for st, d, note_list in chords:
        for offset, n in enumerate(note_list):
            add_piano_note(samples, st + offset * 0.18, d, NOTES[n], amp=0.14)
            
    # Santoor melody floating above
    santoor_notes = [
        (1.0, 0.4, 'G5'), (1.5, 0.4, 'E5'), (2.0, 0.8, 'C5'),
        (4.0, 0.4, 'E5'), (4.5, 0.4, 'C5'), (5.0, 0.8, 'A4'),
        (7.0, 0.4, 'A4'), (7.5, 0.4, 'C5'), (8.0, 0.8, 'F5'),
        (10.0, 0.4, 'G5'), (10.5, 0.4, 'F5'), (11.0, 0.8, 'D5'),
        (13.0, 0.5, 'C5'), (13.6, 0.5, 'E5'), (14.2, 0.9, 'G5'),
        (16.0, 0.5, 'A5'), (16.6, 0.5, 'G5'), (17.2, 0.9, 'E5'),
        (19.0, 0.5, 'F5'), (19.6, 0.5, 'A5'), (20.2, 0.9, 'C6'),
        (22.0, 0.6, 'B5'), (22.8, 0.8, 'G5'), (23.8, 1.8, 'C5')
    ]
    for st, d, n in santoor_notes:
        add_sitar_note(samples, st, d, NOTES[n], amp=0.20)
        
    wav_path = "/tmp/track4.wav"
    mp3_path = "app/src/main/res/raw/romantic_piano_santoor.mp3"
    make_wav(wav_path, samples)
    convert_to_mp3(wav_path, mp3_path)

# --- Track 5: Maharaja Victory Anthem (Festive Festive & Shehnai) ---
def generate_track_5():
    dur = 24.0
    samples = [0.0] * int(dur * SAMPLE_RATE)
    
    # Festive energetic rhythm
    for t in range(0, int(dur * 2)):
        time_sec = t * 0.5
        add_tabla_stroke(samples, time_sec, is_bayan=True, amp=0.20)
        add_tabla_stroke(samples, time_sec + 0.25, is_bayan=False, amp=0.16)
        
    # Shehnai / Royal celebratory melody
    anthem_melody = [
        (0.5, 0.5, 'C5'), (1.0, 0.5, 'E5'), (1.5, 0.5, 'G5'), (2.0, 1.2, 'C6'),
        (3.5, 0.5, 'B5'), (4.0, 0.5, 'A5'), (4.5, 1.2, 'G5'),
        (6.0, 0.5, 'E5'), (6.5, 0.5, 'F5'), (7.0, 0.5, 'G5'), (7.5, 1.2, 'A5'),
        (9.0, 0.5, 'G5'), (9.5, 0.5, 'F5'), (10.0, 1.4, 'E5'),
        (12.0, 0.5, 'C5'), (12.5, 0.5, 'E5'), (13.0, 0.5, 'G5'), (13.5, 1.2, 'C6'),
        (15.0, 0.6, 'D6'), (15.8, 0.6, 'C6'), (16.6, 1.4, 'B5'),
        (18.5, 0.5, 'A5'), (19.0, 0.5, 'B5'), (19.5, 0.5, 'C6'), (20.0, 1.0, 'D6'),
        (21.2, 1.8, 'C6')
    ]
    for st, d, n in anthem_melody:
        # Dual Shehnai harmonic layer
        add_flute_note(samples, st, d, NOTES[n], amp=0.22)
        add_sitar_note(samples, st, d, NOTES[n], amp=0.18)
        
    wav_path = "/tmp/track5.wav"
    mp3_path = "app/src/main/res/raw/maharaja_victory_anthem.mp3"
    make_wav(wav_path, samples)
    convert_to_mp3(wav_path, mp3_path)

# --- Track 6: Mystic Palace Bansuri & Tanpura Meditation ---
def generate_track_6():
    dur = 28.0
    samples = [0.0] * int(dur * SAMPLE_RATE)
    add_tanpura_drone(samples, dur, root_freq=NOTES['D3'], amp=0.08)
    
    # Serene bamboo flute meditation
    bansuri_melody = [
        (0.8, 2.5, 'D4'), (3.5, 2.0, 'F#4'), (5.8, 3.0, 'A4'),
        (9.2, 1.5, 'B4'), (10.9, 1.5, 'C5'), (12.6, 3.2, 'D5'),
        (16.2, 2.0, 'C5'), (18.4, 2.2, 'B4'), (20.8, 2.5, 'A4'),
        (23.5, 3.5, 'D4')
    ]
    for st, d, n in bansuri_melody:
        add_flute_note(samples, st, d, NOTES[n], amp=0.25)
        
    # Soft warm pads underneath
    for cycle in range(7):
        add_piano_note(samples, cycle * 4.0, 3.8, NOTES['D4'], amp=0.08)
        add_piano_note(samples, cycle * 4.0, 3.8, NOTES['A4'], amp=0.06)
        
    wav_path = "/tmp/track6.wav"
    mp3_path = "app/src/main/res/raw/mystic_palace_bansuri.mp3"
    make_wav(wav_path, samples)
    convert_to_mp3(wav_path, mp3_path)

if __name__ == "__main__":
    generate_track_3()
    generate_track_4()
    generate_track_5()
    generate_track_6()
    print("All 4 additional royal tracks generated successfully!")
