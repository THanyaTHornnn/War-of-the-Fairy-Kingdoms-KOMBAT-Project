import { useRef, Suspense } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { useGLTF, OrbitControls, Stage } from "@react-three/drei";

const MODEL_PATHS = {
  verdant:  "/models/verdant.glb",
  celestia: "/models/ce.glb",
  ivy:      "/models/vivy.glb",
  nyx:      "/models/bluedragon.glb",
  mibi:     "/models/Mibi.glb",
};

const GLOW_COLORS = {
  verdant:  "#f9a8d4",
  celestia: "#c4b5fd",
  ivy:      "#86efac",
  nyx:      "#67e8f9",
  mibi:     "#fcd34d",
};

function Model({ minionId, spin }) {
  const ref = useRef();
  const path = MODEL_PATHS[minionId] || MODEL_PATHS.verdant;
  const { scene } = useGLTF(path);

  useFrame((_, delta) => {
    if (spin && ref.current) {
      ref.current.rotation.y += delta * 0.8;
    }
  });

  return <primitive ref={ref} object={scene} scale={1.2} position={[0, -0.4, 0]} />;
}

export default function MinionCharacter3D({
  minionId,
  size = 180,
  spin = true,
  selected = false,
  canDrag = false,
  rounded = true,
}) {
  const glowColor = GLOW_COLORS[minionId] || "#c4b5fd";

  return (
    <div style={{
      width: size,
      height: size,
      borderRadius: rounded ? "50%" : 0,
      overflow: rounded ? "hidden" : "visible",
      boxShadow: selected
        ? `0 0 30px ${glowColor}, 0 0 60px ${glowColor}55`
        : `0 0 15px ${glowColor}55`,
      border: selected ? `2px solid ${glowColor}` : "1px solid rgba(255,255,255,0.2)",
      transition: "box-shadow 0.3s, border 0.3s",
      cursor: canDrag ? "grab" : "default",
    }}>
      <Canvas
        camera={{ position: [0, -0.5, 2], fov: 60}}
        style={{ width: "100%", height: "100%", background: "transparent" }}
        gl={{ alpha: true }}
      >
        <ambientLight intensity={0.6} />
        <directionalLight position={[5, 5, 5]} intensity={1} />
        <pointLight position={[-3, 3, 3]} intensity={0.5} color={glowColor} />
        <Suspense fallback={null}>
          <Model minionId={minionId} spin={spin} />
          {canDrag && <OrbitControls enableZoom={false} enablePan={false} />}
        </Suspense>
      </Canvas>
    </div>
  );
}